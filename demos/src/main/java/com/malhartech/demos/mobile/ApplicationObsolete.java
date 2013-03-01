/**
 * Copyright (c) 2012-2012 Malhar, Inc.
 * All rights reserved.
 */
package com.malhartech.demos.mobile;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.malhartech.api.ApplicationFactory;
import com.malhartech.api.DAG;
import com.malhartech.lib.io.ConsoleOutputOperator;
import com.malhartech.lib.io.HttpInputOperator;
import com.malhartech.lib.io.HttpOutputOperator;
import com.malhartech.lib.testbench.EventIncrementer;
import com.malhartech.lib.testbench.RandomEventGenerator;
import com.malhartech.lib.testbench.SeedEventClassifier;
import com.malhartech.lib.testbench.SeedEventGenerator;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mobile Demo ApplicationObsolete.<p>
 */
public class ApplicationObsolete implements ApplicationFactory
{
  private static final Logger LOG = LoggerFactory.getLogger(ApplicationObsolete.class);
  public static final String P_phoneRange = com.malhartech.demos.mobile.ApplicationObsolete.class.getName() + ".phoneRange";
  private String ajaxServerAddr = null;
  private Range<Integer> phoneRange = Ranges.closed(9000000, 9999999);

  private void configure(Configuration conf)
  {

    this.ajaxServerAddr = System.getenv("MALHAR_AJAXSERVER_ADDRESS");
    LOG.debug(String.format("\n******************* Server address was %s", this.ajaxServerAddr));

    if (LAUNCHMODE_YARN.equals(conf.get(DAG.STRAM_LAUNCH_MODE))) {
      // settings only affect distributed mode
      conf.setIfUnset(DAG.STRAM_CONTAINER_MEMORY_MB.name(), "2048");
      conf.setIfUnset(DAG.STRAM_MASTER_MEMORY_MB.name(), "1024");
      conf.setIfUnset(DAG.STRAM_MAX_CONTAINERS.name(), "1");
    }
    else if (LAUNCHMODE_LOCAL.equals(conf.get(DAG.STRAM_LAUNCH_MODE))) {
    }

    String phoneRange = conf.get(P_phoneRange, null);
    if (phoneRange != null) {
      String[] tokens = phoneRange.split("-");
      if (tokens.length != 2) {
        throw new IllegalArgumentException("Invalid range: " + phoneRange);
      }
      this.phoneRange = Ranges.closed(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
    }
    System.out.println("Phone range: " + this.phoneRange);
  }

  private ConsoleOutputOperator getConsoleOperator(DAG b, String name)
  {
    // output to HTTP server when specified in environment setting
    ConsoleOutputOperator oper = b.addOperator(name, new ConsoleOutputOperator());
    oper.setStringFormat(name + ": %s");
    return oper;
  }

  private HttpOutputOperator<HashMap<String, Object>> getHttpOutputNumberOperator(DAG b, String name)
  {
    // output to HTTP server when specified in environment setting
    String serverAddr =  this.ajaxServerAddr;
    HttpOutputOperator<HashMap<String, Object>> oper = b.addOperator(name, new HttpOutputOperator<HashMap<String, Object>>());
    URI u = URI.create("http://" + serverAddr + "/channel/mobile/" + name);
    oper.setResourceURL(u);
    return oper;
  }

  public SeedEventGenerator getSeedGenerator(String name, DAG b)
  {
    SeedEventGenerator oper = b.addOperator(name, SeedEventGenerator.class);
    // oper.setProperty(SeedEventGenerator.KEY_STRING_SCHEMA, "false");
    oper.setSeedstart(this.phoneRange.lowerEndpoint());
    oper.setSeedend(this.phoneRange.upperEndpoint());
    oper.addKeyData("x", 0, 500);
    oper.addKeyData("y", 0, 500);
    return oper;
  }

  public RandomEventGenerator getRandomGenerator(String name, DAG b)
  {
    RandomEventGenerator oper = b.addOperator(name, RandomEventGenerator.class);
    oper.setMaxvalue(99);
    oper.setMinvalue(0);
    oper.setTuplesBlast(1000);
    oper.setTuplesBlastIntervalMillis(50);
    return oper;
  }

  public SeedEventClassifier<Integer> getSeedClassifier(String name, DAG b)
  {
    SeedEventClassifier<Integer> oper = b.addOperator(name, new SeedEventClassifier<Integer>());
    oper.setSeedstart(this.phoneRange.lowerEndpoint());
    oper.setSeedend(this.phoneRange.upperEndpoint());
    oper.setKey1("x");
    oper.setKey2("y");
    return oper;
  }

  public InvertIndexMapPhone getInvertIndexMap(String name, DAG b)
  {
    InvertIndexMapPhone oper = b.addOperator(name, InvertIndexMapPhone.class);
    return oper;
  }

  public EventIncrementer getIncrementer(String name, DAG b)
  {
    EventIncrementer oper = b.addOperator(name, EventIncrementer.class);
    oper.setDelta(2.0);
    ArrayList<String> klist = new ArrayList<String>(2);
    ArrayList<Double> low = new ArrayList<Double>(2);
    ArrayList<Double> high = new ArrayList<Double>(2);
    klist.add("x");
    klist.add("y");
    low.add(0.0);
    low.add(0.0);
    high.add(500.0);
    high.add(500.0);
    oper.setKeylimits(klist, low, high);
    return oper;
  }

  @Override
  public DAG getApplication(Configuration conf)
  {

    DAG dag = new DAG(conf);
    configure(conf);

    SeedEventGenerator seedGen = getSeedGenerator("seedGen", dag);
    RandomEventGenerator randomXGen = getRandomGenerator("xgen", dag);
    RandomEventGenerator randomYGen = getRandomGenerator("ygen", dag);
    SeedEventClassifier<Integer> seedClassify = getSeedClassifier("seedclassify", dag);
    EventIncrementer incrementer = getIncrementer("incrementer", dag);
    // Operator tupleQueue = getTupleQueue("location_queue", dag);
    InvertIndexMapPhone indexMap = getInvertIndexMap("index_map", dag);

    dag.addStream("seeddata", seedGen.val_list, incrementer.seed).setInline(true);
    dag.addStream("xdata", randomXGen.integer_data, seedClassify.data1).setInline(true);
    dag.addStream("ydata", randomYGen.integer_data, seedClassify.data2).setInline(true);
    dag.addStream("incrdata", seedClassify.hash_data, incrementer.increment).setInline(true);
    dag.addStream("mobilelocation", incrementer.data, indexMap.data).setInline(true);

    if (this.ajaxServerAddr != null) {
      HttpOutputOperator<HashMap<String, Object>> httpconsole = getHttpOutputNumberOperator(dag, "phoneLocationQueryResult");
      dag.addStream("consoledata", indexMap.console, httpconsole.input).setInline(true);

      HttpInputOperator phoneLocationQuery = dag.addOperator("phoneLocationQuery", HttpInputOperator.class);
      URI u = URI.create("http://" + ajaxServerAddr + "/channel/mobile/phoneLocationQuery");
      phoneLocationQuery.setUrl(u);
      dag.addStream("mobilequery", phoneLocationQuery.outputPort, indexMap.query).setInline(true);
    }
    else {
      // for testing purposes without server
      ConsoleOutputOperator phoneconsole = getConsoleOperator(dag, "phoneLocationQueryResult");
      dag.addStream("consoledata", indexMap.console, phoneconsole.input).setInline(true);

      indexMap.setPhoneQuery("idBlah", "9999988");
      indexMap.setPhoneQuery("id102", "9999998");
      indexMap.setLocationQuery("loc1", "34,87");
    }

    return dag;
  }
}