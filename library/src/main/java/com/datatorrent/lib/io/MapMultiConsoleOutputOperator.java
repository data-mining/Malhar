/*
 * Copyright (c) 2013 DataTorrent, Inc. ALL Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datatorrent.lib.io;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datatorrent.api.BaseOperator;
import com.datatorrent.api.DefaultInputPort;

/**
 * 
 * Writes tuples to standard out of the container
 * <p>
 * This is for specific use case for map where I want to print each key value
 * pair in different line <br>
 * Mainly to be used for debugging. Users should be careful to not have this
 * node listen to a high throughput stream<br>
 * <br>
 * 
 * @since 0.3.4
 */
public class MapMultiConsoleOutputOperator<K, V> extends BaseOperator
{
  private boolean debug = false;

  public boolean isDebug()
  {
    return debug;
  }

  public void setDebug(boolean debug)
  {
    this.debug = debug;
  }

  private static final Logger logger = LoggerFactory.getLogger(MapMultiConsoleOutputOperator.class);
  public final transient DefaultInputPort<Map<K, V>> input = new DefaultInputPort<Map<K, V>>() {
    @Override
    public void process(Map<K, V> t)
    {
      System.out.println("{");
      for (Map.Entry<K, V> entry : t.entrySet()) {
        if (!silent) {
          System.out.println(entry.getKey().toString() + "=" + entry.getValue().toString());
        }
        if (debug)
          logger.info(entry.getKey().toString() + "=" + entry.getValue().toString());
      }
      System.out.println("}");
    }
  };

  boolean silent = false;

  public boolean isSilent()
  {
    return silent;
  }

  public void setSilent(boolean silent)
  {
    this.silent = silent;
  }

}
