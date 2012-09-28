/*
 *  Copyright (c) 2012 Malhar, Inc.
 *  All Rights Reserved.
 */
package com.malhartech.lib.testbench;

import com.malhartech.annotation.ModuleAnnotation;
import com.malhartech.annotation.PortAnnotation;
import com.malhartech.dag.AbstractModule;
import com.malhartech.dag.FailedOperationException;
import com.malhartech.dag.ModuleConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes in a seed stream on port <b>seed</b> and then on increments this data based on increments on port <b>increment</b>.
 * Data is immediately emitted on output port <b>data</b>.<p>
 * The aim is to create a random movement
 * <br>
 * Examples of application includes<br>
 * random motion<br>
 * <br>
 * <br>
 * Description: tbd
 * <br>
 * Benchmarks: This node has been benchmarked at over ?? million tuples/second in local/inline mode<br>
 *
 * <b>Tuple Schema</b>: Each tuple is HashMap<String, ArrayList> on both the ports. Currently other schemas are not supported<br>
 * <b>Port Interface</b><br>
 * <b>seed</b>: The seed data for setting up the incrementor data to work on<br>
 * <b>increment</b>: Small random increments to the seed data. This now creates a randomized change in the seed<br>
 * <b>data</b>: Output of seed + increment<br>
 * <br>
 * <b>Properties</b>:
 * <br>keys: In case the value has multiple dimensions. They can be accessed via keys<br>
 * <br>delta: The max value from an increment. The value on increment port is treated as a "percent" of this delta<br>
 * Compile time checks are:<br>
 * <br>
 *
 * @author amol
 */
@ModuleAnnotation(
        ports = {
  @PortAnnotation(name = LoadIncrementor.IPORT_SEED, type = PortAnnotation.PortType.INPUT),
  @PortAnnotation(name = LoadIncrementor.IPORT_INCREMENT, type = PortAnnotation.PortType.INPUT),
  @PortAnnotation(name = LoadIncrementor.OPORT_DATA, type = PortAnnotation.PortType.OUTPUT)
})
public class LoadIncrementor extends AbstractModule
{
  public static final String IPORT_SEED = "seed";
  public static final String IPORT_INCREMENT = "increment";
  public static final String OPORT_DATA = "data";
  private static Logger LOG = LoggerFactory.getLogger(LoadIncrementor.class);

  HashMap<String, Object> vmap = new HashMap<String, Object>();

/**
   * keys are comma separated list of keys for seeding. They are taken in order on seed port (i.e. keys need not be sent)<p>
   * On the increment port changes are sent per key.<br>
   * If not provided the keys are ignored (i.e. behaves like single value)<br>
   *
   */
  public static final String KEY_KEYS = "keys";

/**
   * delta defines what constitutes a change. Default value is 1<p>
   * <br>
   */

  public static final String KEY_DELTA = "delta";

  // Data Recieved on seed port
  class valueData
  {
    String str;
    Object value;

    valueData(String istr, Object val)
    {
      str = istr;
      value = val;
    }
  }

  /**
   *
   * Code to be moved to a proper base method name
   *
   * @param config
   * @return boolean
   */
  public boolean myValidation(ModuleConfiguration config)
  {
    boolean ret = true;

    return ret;
  }

  /**
   * Sets up all the config parameters. Assumes checking is done and has passed
   *
   * @param config
   */
  @Override
  public void setup(ModuleConfiguration config) throws FailedOperationException
  {
    if (!myValidation(config)) {
      throw new FailedOperationException("Did not pass validation");
    }
  }

  /**
   * Process each tuple
   *
   * @param payload
   */
  @Override
  public void process(Object payload)
  {
    // LoadSeedGenerator would provide seed
    // LoadRandomGenerator->SeedClassifier would provide Increment, use delta to make it fit


    if (IPORT_SEED.equals(getActivePort())) {
      // Payload is     HashMap<String, Object> ret = new HashMap<String, Object>();, where Object is ArrayList of Integers
      // Allow Seed to override
      for (Map.Entry<String, ArrayList> e: ((HashMap<String, ArrayList>)payload).entrySet()) {
        ArrayList alist = new ArrayList();
        for (valueData n : (ArrayList<valueData>) e.getValue()) {
          alist.add(new valueData(n.str, n.value));
        }
        vmap.put(e.getKey(), alist);
      }
    }
    else if (IPORT_INCREMENT.equals(getActivePort())) {
      for (Map.Entry<String, Object> e: ((HashMap<String, Object>) payload).entrySet()) {
        String key = e.getKey();
        ArrayList alist = (ArrayList) vmap.get(key);
        if (alist != null) {
          for (Map.Entry<String, Integer> o : ((HashMap<String, Integer>) e.getValue()).entrySet()) {
            // Now look at the (x and val) or (y and val)

          }
        }
      }
    }
  }
}