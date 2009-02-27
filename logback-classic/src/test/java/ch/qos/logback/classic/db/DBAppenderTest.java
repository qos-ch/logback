package ch.qos.logback.classic.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.db.DriverManagerConnectionSource;

public class DBAppenderTest  {

  LoggerContext lc;
  Logger logger;
  DBAppender appender;
  DriverManagerConnectionSource connectionSource;

  static DBAppenderTestFixture DB_APPENDER_TEST_FIXTURE;
  
  @BeforeClass
  public  static  void fixtureSetUp() throws SQLException {
    DB_APPENDER_TEST_FIXTURE = new DBAppenderTestFixture();
    DB_APPENDER_TEST_FIXTURE.setUp();
  }
  
  @AfterClass
  public static void fixtureTearDown() throws SQLException {
    DB_APPENDER_TEST_FIXTURE.tearDown();
  }
  
  @Before
  public void setUp() throws SQLException {
    lc = new LoggerContext();
    lc.setName("default");
    logger = lc.getLogger("root");
    appender = new DBAppender();
    appender.setName("DB");
    appender.setContext(lc);
    connectionSource = new DriverManagerConnectionSource();
    connectionSource.setContext(lc);
    connectionSource.setDriverClass(DBAppenderTestFixture.HSQLDB_DRIVER_CLASS);
    connectionSource.setUrl(DB_APPENDER_TEST_FIXTURE.url);
    connectionSource.setUser(DB_APPENDER_TEST_FIXTURE.user);
    connectionSource.setPassword(DB_APPENDER_TEST_FIXTURE.password);
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
  }

  @Test
  public void testAppendLoggingEvent() throws SQLException {
    ILoggingEvent event = createLoggingEvent();

    appender.append(event);
    //StatusPrinter.print(lc.getStatusManager());
    
    Statement stmt = connectionSource.getConnection().createStatement();
    ResultSet rs = null;
    rs = stmt.executeQuery("SELECT * FROM logging_event");
    if (rs.next()) {
      assertEquals(event.getTimeStamp(), rs.getLong(1));
      assertEquals(event.getFormattedMessage(), rs.getString(2));
      assertEquals(event.getLoggerName(), rs.getString(3));
      assertEquals(event.getLevel().toString(), rs.getString(4));
      assertEquals(event.getThreadName(), rs.getString(5));
      assertEquals(DBHelper.computeReferenceMask(event), rs.getShort(6));
      CallerData callerData = event.getCallerData()[0];
      assertEquals(callerData.getFileName(), rs.getString(7));
      assertEquals(callerData.getClassName(), rs.getString(8));
      assertEquals(callerData.getMethodName(), rs.getString(9));
    } else {
      fail("No row was inserted in the database");
    }
    
    rs.close();
    stmt.close();
  }
  
  public void testAppendThrowable() throws SQLException {
    ILoggingEvent event = createLoggingEvent();

    appender.append(event);
    
    Statement stmt = connectionSource.getConnection().createStatement();
    ResultSet rs = null;
    rs = stmt.executeQuery("SELECT * FROM logging_event_exception where event_id = 0");
    int i = 0;
    while (rs.next()) {
      assertEquals(event.getThrowableProxy().getThrowableDataPointArray()[i].toString(), rs.getString(3));
      i++;
    }
    
    rs.close();
    stmt.close();
  }
  
  public void testContextInfo() throws SQLException {
    ILoggingEvent event = createLoggingEvent();
    lc.putProperty("testKey1", "testValue1");
    
    appender.append(event);
    
    Statement stmt = connectionSource.getConnection().createStatement();
    ResultSet rs = null;
    rs = stmt.executeQuery("SELECT * FROM logging_event_property where event_id = 0");
    Map<String, String> map = appender.mergePropertyMaps(event);
    while (rs.next()) {
      String key = rs.getString(2);
      assertEquals(map.get(key), rs.getString(3));
      //System.out.println("value: " + map.get(key));
    }
    
    rs.close();
    stmt.close();
  }
  
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
    ILoggingEvent le = new LoggingEvent(this.getClass().getName(), logger,
        Level.DEBUG, "test message", new Exception("test Ex"), null);
    return le;
  }
}
