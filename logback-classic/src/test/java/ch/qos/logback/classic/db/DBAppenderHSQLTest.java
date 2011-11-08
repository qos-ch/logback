/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.log4j.MDC;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.db.DriverManagerConnectionSource;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusPrinter;

public class DBAppenderHSQLTest  {

  LoggerContext lc;
  Logger logger;
  DBAppender appender;
  DriverManagerConnectionSource connectionSource;

  DBAppenderHSQLTestFixture dbAppenderHSQLTestFixture;
  int diff = RandomUtil.getPositiveInt();
  
  @Before
  public void setUp() throws SQLException {
    dbAppenderHSQLTestFixture = new DBAppenderHSQLTestFixture();
    dbAppenderHSQLTestFixture.setUp();

    lc = new LoggerContext();
    lc.setName("default");
    logger = lc.getLogger("root");
    appender = new DBAppender();
    appender.setName("DB");
    appender.setContext(lc);
    connectionSource = new DriverManagerConnectionSource();
    connectionSource.setContext(lc);
    connectionSource.setDriverClass(DBAppenderHSQLTestFixture.HSQLDB_DRIVER_CLASS);
    connectionSource.setUrl(dbAppenderHSQLTestFixture.url);
    connectionSource.setUser(dbAppenderHSQLTestFixture.user);
    connectionSource.setPassword(dbAppenderHSQLTestFixture.password);
    connectionSource.start();
    appender.setConnectionSource(connectionSource);
    appender.start();
  }
  
  @After
  public void tearDown() throws SQLException {
    logger = null;
    lc = null;
    appender = null;
    connectionSource = null;
    dbAppenderHSQLTestFixture.tearDown();
  }

  @Test
  public void testAppendLoggingEvent() throws SQLException {
    ILoggingEvent event = createLoggingEvent();

    appender.append(event);
    StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    
    Statement stmt = connectionSource.getConnection().createStatement();
    ResultSet rs = null;
    rs = stmt.executeQuery("SELECT * FROM logging_event");
    if (rs.next()) {
      assertEquals(event.getTimeStamp(), rs.getLong(DBAppender.TIMESTMP_INDEX));
      assertEquals(event.getFormattedMessage(), rs.getString(DBAppender.FORMATTED_MESSAGE_INDEX));
      assertEquals(event.getLoggerName(), rs.getString(DBAppender.LOGGER_NAME_INDEX));
      assertEquals(event.getLevel().toString(), rs.getString(DBAppender.LEVEL_STRING_INDEX));
      assertEquals(event.getThreadName(), rs.getString(DBAppender.THREAD_NAME_INDEX));
      assertEquals(DBHelper.computeReferenceMask(event), rs.getShort(DBAppender.REFERENCE_FLAG_INDEX));
      assertEquals(String.valueOf(diff), rs.getString(DBAppender.ARG0_INDEX));
      StackTraceElement callerData = event.getCallerData()[0];
      assertEquals(callerData.getFileName(), rs.getString(DBAppender.CALLER_FILENAME_INDEX));
      assertEquals(callerData.getClassName(), rs.getString(DBAppender.CALLER_CLASS_INDEX));
      assertEquals(callerData.getMethodName(), rs.getString(DBAppender.CALLER_METHOD_INDEX));
    } else {
      fail("No row was inserted in the database");
    }
    
    rs.close();
    stmt.close();
  }
  
  @Test
  public void testAppendThrowable() throws SQLException {
    ILoggingEvent event = createLoggingEvent();

    appender.append(event);
    
    Statement stmt = connectionSource.getConnection().createStatement();
    ResultSet rs = null;
    rs = stmt.executeQuery("SELECT * FROM LOGGING_EVENT_EXCEPTION where EVENT_ID = 0");
    
    rs.next();
    String expected = "java.lang.Exception: test Ex";
    String firstLine = rs.getString(3);
    assertTrue("["+firstLine+"] does not match ["+expected+"]", firstLine.contains(expected));
    
    int i = 0;
    while (rs.next()) {
      expected = event.getThrowableProxy().getStackTraceElementProxyArray()[i].toString();
      String st = rs.getString(3);
      assertTrue("["+st+"] does not match ["+expected+"]", st.contains(expected));
      i++;
    }
    assertTrue(i != 0);
    rs.close();
    stmt.close();
  }
  
  @Test
  public void testContextInfo() throws SQLException {
    lc.putProperty("testKey1", "testValue1");
    MDC.put("k"+diff, "v"+diff);
    ILoggingEvent event = createLoggingEvent();
    
    appender.append(event);
    
    Statement stmt = connectionSource.getConnection().createStatement();
    ResultSet rs = null;
    rs = stmt.executeQuery("SELECT * FROM LOGGING_EVENT_PROPERTY  WHERE EVENT_ID = 0");
    Map<String, String> map = appender.mergePropertyMaps(event);
    System.out.println("ma.size="+map.size());
    int i = 0;
    while (rs.next()) {
      String key = rs.getString(2);
      assertEquals(map.get(key), rs.getString(3));
      i++;
    }
    assertTrue(map.size() != 0);
    assertEquals(map.size(), i);
    rs.close();
    stmt.close();
  }
  
  @Test
  public void testAppendMultipleEvents() throws SQLException {
    for (int i = 0; i < 10; i++) {
      ILoggingEvent event = createLoggingEvent();
      appender.append(event);
    }
    
    Statement stmt = connectionSource.getConnection().createStatement();
    ResultSet rs = null;
    rs = stmt.executeQuery("SELECT * FROM logging_event");
    int count = 0;
    while (rs.next()) {
      count++;
    }
    assertEquals(10, count);
    
    rs.close();
    stmt.close();
  }


  private ILoggingEvent createLoggingEvent() {
    return new LoggingEvent(this.getClass().getName(), logger,
        Level.DEBUG, "test message", new Exception("test Ex"), new Integer[]{diff});
  }
}
