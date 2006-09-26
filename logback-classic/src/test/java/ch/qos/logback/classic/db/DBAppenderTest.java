package ch.qos.logback.classic.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.LoggingEvent;

public class DBAppenderTest extends DBAppenderTestBase {

  LoggerContext lc;
  Logger logger;
  DBAppender appender;
  DriverManagerConnectionSource connectionSource;

  public DBAppenderTest(String name) {
    super(name);
  }

  public void setUp() throws SQLException {
    super.setUp();
    lc = new LoggerContext();
    lc.setName("default");
    logger = lc.getLogger("root");
    appender = new DBAppender();
    appender.setName("DB");
    appender.setContext(lc);
    connectionSource = new DriverManagerConnectionSource();
    connectionSource.setContext(lc);
    connectionSource.setDriverClass(DRIVER_CLASS);
    connectionSource.setUrl(url);
    connectionSource.setUser(user);
    connectionSource.setPassword(password);
    connectionSource.start();
    appender.setConnectionSource(connectionSource);
    appender.start();
  }
  
  public void tearDown() throws SQLException {
    super.tearDown();
    logger = null;
    lc = null;
    appender = null;
    connectionSource = null;
  }

  public void testAppendLoggingEvent() throws SQLException {
    LoggingEvent event = createLoggingEvent();

    appender.append(event);
    //StatusPrinter.print(lc.getStatusManager());
    
    Statement stmt = connectionSource.getConnection().createStatement();
    ResultSet rs = null;
    rs = stmt.executeQuery("SELECT * FROM logging_event");
    if (rs.next()) {
      assertEquals(event.getTimeStamp(), rs.getLong(1));
      assertEquals(event.getFormattedMessage(), rs.getString(2));
      assertEquals(event.getLoggerRemoteView().getName(), rs.getString(3));
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
    LoggingEvent event = createLoggingEvent();

    appender.append(event);
    //StatusPrinter.print(lc.getStatusManager());
    
    Statement stmt = connectionSource.getConnection().createStatement();
    ResultSet rs = null;
    rs = stmt.executeQuery("SELECT * FROM logging_event_exception where event_id = 0");
    int i = 0;
    while (rs.next()) {
      assertEquals(event.getThrowableInformation().getThrowableStrRep()[i], rs.getString(3));
      i++;
    }
    
    rs.close();
    stmt.close();
  }
  
  public void testContextInfo() throws SQLException {
    LoggingEvent event = createLoggingEvent();
    lc.setProperty("testKey1", "testValue1");
    
    appender.append(event);
    //StatusPrinter.print(lc.getStatusManager());
    
    Statement stmt = connectionSource.getConnection().createStatement();
    ResultSet rs = null;
    rs = stmt.executeQuery("SELECT * FROM logging_event_property where event_id = 0");
    Map<String, String> map = appender.mergePropertyMaps(event);
    while (rs.next()) {
      String key = rs.getString(2);
      assertEquals(map.get(key), rs.getString(3));
      System.out.println("value: " + map.get(key));
    }
    
    rs.close();
    stmt.close();
  }
  
  public void testAppendMultipleEvents() throws SQLException {
    for (int i = 0; i < 10; i++) {
      LoggingEvent event = createLoggingEvent();
      appender.append(event);
    }
    //StatusPrinter.print(lc.getStatusManager());
    
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
  

  private LoggingEvent createLoggingEvent() {
    LoggingEvent le = new LoggingEvent(this.getClass().getName(), logger,
        Level.DEBUG, "test message", new Exception("test Ex"), null);
    return le;
  }
}
