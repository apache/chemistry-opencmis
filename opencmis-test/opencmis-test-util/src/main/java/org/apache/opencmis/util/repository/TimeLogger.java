/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.opencmis.util.repository;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple utility class for time logging
 * Note: NOT thread safe!
 * 
 * @author Jens
 *
 */
public class TimeLogger {
  private static Log LOG = LogFactory.getLog(TimeLogger.class);

  private static class TimeRecord {
    public long fStart;
    public long fStop;    
  };
  
  private String fAction;
  private LinkedList<TimeRecord> fTimeRecs = new LinkedList<TimeRecord>();
  private final int maxSize = 2500;
  TimeRecord fCurrentRec;
  
  public TimeLogger() {
    fAction = "";
    fTimeRecs = new LinkedList<TimeRecord>();
  }
  
  public TimeLogger(String action) {
    fAction = action;
  }
  
  public void start() {
    createAndAddNewRecord();    
    fCurrentRec.fStart = System.currentTimeMillis();    
  }
  
  public void stop() {
    fCurrentRec.fStop = System.currentTimeMillis();
  }
  
  public void reset() {
    fTimeRecs.clear();
  }
  
  public void logTimes() {
    long size =  fTimeRecs.size();
    if (size==0)
      LOG.info("No samples for " + fAction + " available. ");
    else if (size==1)
      LOG.info("Time elapsed for " + fAction + ": " + getLastTime());
    else {
      LOG.info("Timings for " + size + " samples for action " + fAction + ": ");
      LOG.info("  Average: " + getAverageTime() + "ms");
      LOG.info("  Min    : " + getMinTime() + "ms");
      LOG.info("  Max    : " + getMaxTime() + "ms");
      LOG.info("  Total  : " + getTotalTime() + "ms");
    }      
  }
  
  public void printTimes() {
    long size =  fTimeRecs.size();
    if (size==0)
      System.out.println("No samples for " + fAction + " available. ");
    else if (size==1)
      System.out.println("Time elapsed for " + fAction + ": " + getLastTime());
    else {
      System.out.println("Timings for " + size + " samples for action " + fAction + ": ");
      System.out.println("  Average: " + getAverageTime() + "ms");
      System.out.println("  Min    : " + getMinTime() + "ms");
      System.out.println("  Max    : " + getMaxTime() + "ms");
      System.out.println("  Total  : " + getTotalTime() + "ms");
    }
  }

  public long getLastTime() {
    TimeRecord lastRec = fTimeRecs.getLast();
    if (null != lastRec)
      return lastRec.fStop-lastRec.fStart;
    else
      return 0;
  }
  
  private void createAndAddNewRecord() {
    if (fTimeRecs.size() < maxSize) {
      fCurrentRec = new TimeRecord();
      fTimeRecs.add(fCurrentRec);
    }
  }
  
  private long getAverageTime() {
    long sum = 0;
    long size =  fTimeRecs.size();
    
    if (0 == fTimeRecs.size())
      return 0;
    
    for (TimeRecord tm : fTimeRecs) {
      sum += tm.fStop - tm.fStart;
    }
    return (sum + size/2) / size; 
  }
  
  private long getMinTime() {
    long min = Long.MAX_VALUE;
    
    if (0 == fTimeRecs.size())
      return 0;
    
    for (TimeRecord tm : fTimeRecs) {
      long val = tm.fStop - tm.fStart;
      if (val < min)
       min = val;
    }
    return min; 
    
  }

  private long getMaxTime() {
    long max = Long.MIN_VALUE;
    
    if (0 == fTimeRecs.size())
      return 0;
    
    for (TimeRecord tm : fTimeRecs) {
      long val = tm.fStop - tm.fStart;
      if (val > max)
       max = val;
    }
    return max; 
  }
  
  private long getTotalTime() {
    long sum = 0;
    
    for (TimeRecord tm : fTimeRecs) {
      sum = tm.fStop - tm.fStart;
    }
    return sum; 
  }

}
