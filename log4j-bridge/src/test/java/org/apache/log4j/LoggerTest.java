package org.apache.log4j;

import junit.framework.TestCase;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * A class that tests the invocation of the org.apache.log4j.Logger class that
 * belongs to the log4j-bridge package
 * 
 * @author S&eacute;bastien Pennec
 * @author Ceki G&uuml;lc&uuml;
 */

public class LoggerTest extends TestCase {

  LoggerContext context;
  ListAppender<LoggingEvent> appender;
  ch.qos.logback.classic.Logger logbackLogger;
  org.apache.log4j.Logger log4jLogger;

  public void setUp() throws Exception {
    context = (LoggerContext) LoggerFactory.getILoggerFactory();
    context.shutdownAndReset();
    appender = new ListAppender<LoggingEvent>();
    appender.setContext(context);
    appender.setName("listAppender");
    appender.start();
    ch.qos.logback.classic.Logger lbLogger = context
        .getLogger(LoggerContext.ROOT_NAME);
    lbLogger.addAppender(appender);

    log4jLogger = org.apache.log4j.Logger.getLogger(LoggerTest.class);
    logbackLogger = context.getLogger(LoggerTest.class);
    super.setUp();
  }

  public void tearDown() throws Exception {
    appender.stop();
    context.stop();
    appender = null;
    context = null;
    logbackLogger = null;
    log4jLogger = null;
    super.tearDown();
  }

  public void testLogWithObjectMessages() {
    LoggingEvent event;

    log4jLogger.debug("test");
    event = appender.list.get(0);
    assertEquals("test", event.getMessage());
    appender.list.clear();

    log4jLogger.debug(null);
    event = appender.list.get(0);
    assertEquals(null, event.getMessage());
    appender.list.clear();

    DummyObject dummy = new DummyObject();
    log4jLogger.debug(dummy);
    event = appender.list.get(0);
    assertEquals(dummy.toString(), event.getMessage());
    appender.list.clear();
  }

  public void testIsEnabledAPI() {
    assertFalse(log4jLogger.isTraceEnabled());
    assertTrue(log4jLogger.isDebugEnabled());
    assertTrue(log4jLogger.isInfoEnabled());
    assertTrue(log4jLogger.isWarnEnabled());
    assertTrue(log4jLogger.isErrorEnabled());
  }

  public void testPrintAPI() {
    Exception e = new Exception("just testing");

    log4jLogger.trace(null);
    assertEquals(1, appender.list.size());
    appender.list.clear();
    
    log4jLogger.debug(null);
    assertEquals(1, appender.list.size());
    appender.list.clear();

    log4jLogger.debug("debug message");
    assertEquals(1, appender.list.size());
    appender.list.clear();

    log4jLogger.info(null);
    assertEquals(1, appender.list.size());
    appender.list.clear();

    log4jLogger.info("info  message");
    assertEquals(1, appender.list.size());
    appender.list.clear();

    log4jLogger.warn(null);
    assertEquals(1, appender.list.size());
    appender.list.clear();

    log4jLogger.warn("warn message");
    assertEquals(1, appender.list.size());
    appender.list.clear();

    log4jLogger.error(null);
    assertEquals(1, appender.list.size());
    appender.list.clear();

    log4jLogger.error("error message");
    assertEquals(1, appender.list.size());
    appender.list.clear();

    log4jLogger.debug(null, e);
    assertEquals(1, appender.list.size());
    appender.list.clear();

    log4jLogger.debug("debug message", e);
    assertEquals(1, appender.list.size());
    appender.list.clear();

    log4jLogger.info(null, e);
    assertEquals(1, appender.list.size());
    appender.list.clear();

    log4jLogger.info("info  message", e);
    assertEquals(1, appender.list.size());
    appender.list.clear();

    log4jLogger.warn(null, e);
    assertEquals(1, appender.list.size());
    appender.list.clear();

    log4jLogger.warn("warn message", e);
    assertEquals(1, appender.list.size());
    appender.list.clear();

    log4jLogger.error(null, e);
    assertEquals(1, appender.list.size());
    appender.list.clear();

    log4jLogger.error("error message", e);
    assertEquals(1, appender.list.size());
    appender.list.clear();

  }
  
  public void testLogAPI() {
    log4jLogger.log("x", Level.TRACE, "x", null);
    assertEquals(0, appender.list.size());

    log4jLogger.log("x", Level.DEBUG, "x", null);
    log4jLogger.log("x", Level.INFO, "x", null);
    log4jLogger.log("x", Level.WARN, "x", null);
    log4jLogger.log("x", Level.ERROR, "x", null);
    log4jLogger.log("x", Level.FATAL, "x", null);

    assertEquals(5, appender.list.size());
    appender.list.clear();

  }

}
