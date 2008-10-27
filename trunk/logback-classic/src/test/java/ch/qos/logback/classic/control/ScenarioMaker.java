/** 
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2005, QOS.ch, LOGBack.com
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.control;

import java.util.LinkedList;

import ch.qos.logback.classic.ClassicGlobal;

public class ScenarioMaker {


  private final static int AVERAGE_LOGGER_DEPTH = 4;
  private final static int LOGGER_DEPT_DEV = 2;

  /**
   * Makes a scenario with len logger creations. Logger names are generated independently such that the overwhelming
   * majority of logger names will unrelated to each other. Each logger creation may be followed with a randomly
   * generated set levelInt action on that logger.
   *
   * @param len
   * @return
   */
  static public Scenario makeTypeAScenario(int len) {
    Scenario scenario = new Scenario();
    ;
    for (int i = 0; i < len; i++) {
      String loggerName = RandomUtil.randomLoggerName(AVERAGE_LOGGER_DEPTH, LOGGER_DEPT_DEV);
      scenario.addAction(new CreateLogger(loggerName));
    }
    return scenario;
  }

  static public Scenario makeTypeBScenario(int len) {
    Scenario scenario = new Scenario();
    LinkedList<String> queue = new LinkedList<String>();
    int loggerCreationCount = 0;

    // add an empty string to get going
    queue.add("");

    // add another string to reduce the probability of having an 
    // empty queue (this happens when we create several leaf nodes
    // successively
    queue.add("xxxx");
    
    while (loggerCreationCount < len) {
      if (queue.isEmpty()) {
        throw new IllegalStateException("Queue cannot be empty.");
      }

      String loggerName = (String) queue.removeFirst();
      //System.out.println("logger name is [" + loggerName + "]");
      int childrenCount = RandomUtil.randomChildrenCount(loggerName);
      //System.out.println("children count is " + childrenCount);
      // add only leaf loggers
      if (childrenCount == 0) {
        scenario.addAction(new CreateLogger(loggerName));
        loggerCreationCount++;
      } else {
        for (int i = 0; i < childrenCount; i++) {
          String childName;
          if (loggerName.equals("")) {
            childName = RandomUtil.randomId();
          } else {
            childName = loggerName + ClassicGlobal.LOGGER_SEPARATOR + RandomUtil.randomId();
          }
          queue.add(childName);
        }
      }
    }
    return scenario;
  }
}