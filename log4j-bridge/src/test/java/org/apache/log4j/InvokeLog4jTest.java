/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
*/

package org.apache.log4j;

import junit.framework.TestCase;

/**
 * A class that tests the invocation of the org.apache.log4j.Logger class
 * that belongs to the log4j-bridge package
 *
 * @author S&eacute;bastien Pennec
 * @author Ceki G&uuml;lc&uuml;
 */
public class InvokeLog4jTest extends TestCase {

  public void testIsEnabledAPI() {
    Logger log = Logger.getLogger(InvokeLog4jTest.class.getName());
    
    assertTrue(log.isTraceEnabled());
    assertTrue(log.isDebugEnabled());
    assertTrue(log.isInfoEnabled());
    assertTrue(log.isWarnEnabled());
    assertTrue(log.isErrorEnabled());
  }
  
  public void testPrintAPI() {
    Logger log = Logger.getLogger(InvokeLog4jTest.class.getName());
    Exception e = new Exception("just testing");
    
    log.debug(null);
    log.debug("debug message");
    
    log.info(null);
    log.info("info  message");
    
    log.warn(null);
    log.warn("warn message");

    log.error(null);
    log.error("error message");
    
    log.debug(null, e);
    log.debug("debug message", e);
    
    log.info(null, e);    
    log.info("info  message", e);
    
    log.warn(null, e);
    log.warn("warn message", e);
    
    log.error(null, e);
    log.error("error message", e);
  }
}
