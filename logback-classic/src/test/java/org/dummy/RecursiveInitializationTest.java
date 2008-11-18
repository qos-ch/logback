package org.dummy;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;

public class RecursiveInitializationTest {

  int diff = new Random().nextInt();

  @Before
  public void setUp() throws Exception {
    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY,
        "recursiveInit.xml");
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
    StatusManager sm = loggerContext.getStatusManager();

    assertEquals("Initialization should proceed without errors:", sm.getLevel(),
        Status.INFO);
  }

}
