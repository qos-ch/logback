package org.slf4j.impl;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.List;
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


public class RecursiveInitializationTest {

  int diff = new Random().nextInt();

  @Before
  public void setUp() throws Exception {
    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY,
        "recursiveInit.xml");
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
    StatusManager sm = loggerContext.getStatusManager();

    assertEquals("Was expecting no errors", Status.INFO, sm.getLevel());
    
    List<Status> statusList = sm.getCopyOfStatusList();
//    int errorCount = 0;
//    
//    for(Status s: statusList) {
//      if(s.getLevel() == Status.ERROR) {
//        errorCount++;
//        System.out.println("==========================");
//        System.out.println(s);
//        System.out.println("==========================");
//        
//        assertNull("Status ["+s+"] has a throwable", s.getThrowable());
//      }
//    }
    // Error msg: No appenders present in context [default] for logger [ResursiveLBAppender..].
//    assertEquals("Expecting only one error", 1, errorCount);
    }

}
