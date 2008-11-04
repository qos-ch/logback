package org.dummy;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;

public class Log4jInvocation {

  static final String HELLO = "Hello";

  DummyLBAppender listAppender;
  LoggerContext lc;
  ch.qos.logback.classic.Logger rootLogger;
  
  @Before
  public void fixture() {
    lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.reset();

    listAppender = new DummyLBAppender();
    listAppender.setContext(lc);
    listAppender.start();
    rootLogger = lc.getLogger("root");
    rootLogger.addAppender(listAppender);
  }

  @Test
  public void basic() {
    assertEquals(0, listAppender.list.size());

    Logger logger = Logger.getLogger("basic-test");
    logger.debug(HELLO);

    assertEquals(1, listAppender.list.size());
    LoggingEvent event = (LoggingEvent) listAppender.list.get(0);
    assertEquals(HELLO, event.getMessage());
  }

  @Test
  public void callerData() {
    assertEquals(0, listAppender.list.size());

    PatternLayout pl = new PatternLayout();
    pl.setPattern("%-5level [%class] %logger - %msg");
    pl.setContext(lc);
    pl.start();
    listAppender.layout = pl;

    Logger logger = Logger.getLogger("basic-test");
    logger.trace("none");
    assertEquals(0, listAppender.list.size());
    
    rootLogger.setLevel(Level.TRACE);
    logger.trace(HELLO);
    assertEquals(1, listAppender.list.size());

    LoggingEvent event = (LoggingEvent) listAppender.list.get(0);
    assertEquals(HELLO, event.getMessage());

    assertEquals(1, listAppender.stringList.size());
    assertEquals("TRACE [" + Log4jInvocation.class.getName()
        + "] basic-test - Hello", listAppender.stringList.get(0));
  }
}
