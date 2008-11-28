package org.slf4j.impl;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.StatusPrinter;

public class RecursiveInitializationTest {

  int diff = new Random().nextInt();

  @Before
  public void setUp() throws Exception {
    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY,
        "recursiveInit.xml");
    StaticLoggerBinderFriend.reset();
    LoggerFactoryFriend.reset();

  }

  @After
  public void tearDown() throws Exception {
    System.clearProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
  }

  @Test
  public void recursiveLogbackInitialization() {
    Logger logger = LoggerFactory.getLogger("RecursiveInitializationTest"
        + diff);
    logger.info("RecursiveInitializationTest");

    LoggerContext loggerContext = (LoggerContext) LoggerFactory
        .getILoggerFactory();
    StatusPrinter.printIfErrorsOccured(loggerContext);
    StatusManager sm = loggerContext.getStatusManager();
    assertEquals("Was expecting no errors", Status.WARN, sm.getLevel());
  }

}
