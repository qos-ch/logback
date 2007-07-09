/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package test;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

/**
 * 
 * A test case that issues the typical calls
 * that an application using log4j 1.3 would do.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */

public class Log4j13Calls extends TestCase {
  public static final Logger logger = Logger.getLogger(Log4j12Calls.class);
  
  public void testLog() {
    MDC.put("key", "value1");
    
    logger.trace("Trace level can be noisy");
    logger.debug("Entering application");
    logger.info("Violets are blue");
    logger.warn("Here is a warning");
    logger.info("The answer is {}.", new Integer(42));
    logger.info("Number: {} and another one: {}.", new Integer(42), new Integer(24));
    
    logger.error("Exiting application", new Exception("just testing"));
    
    MDC.remove("key");
    
    MDC.clear();
  }
}
