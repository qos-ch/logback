package ch.qos.logback.access.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ch.qos.logback.access.pattern.helpers.DummyRequest;
import ch.qos.logback.access.pattern.helpers.DummyResponse;
import ch.qos.logback.access.pattern.helpers.DummyServerAdapter;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.BasicContext;
import ch.qos.logback.core.db.DriverManagerConnectionSource;
import ch.qos.logback.core.util.StatusPrinter;

public class DBAppenderTest extends DBAppenderTestBase {

  BasicContext context;
  DBAppender appender;
  DriverManagerConnectionSource connectionSource;

  public DBAppenderTest(String name) {
    super(name);
  }

  public void setUp() throws SQLException {
    super.setUp();
    context = new BasicContext();
    context.setName("default");
    appender = new DBAppender();
    appender.setName("DB");
    appender.setContext(context);
    connectionSource = new DriverManagerConnectionSource();
    connectionSource.setContext(context);
    connectionSource.setDriverClass(DRIVER_CLASS);
    connectionSource.setUrl(url);
    connectionSource.setUser(user);
    connectionSource.setPassword(password);
    connectionSource.start();
    appender.setConnectionSource(connectionSource);
  }
  
  private void setInsertHeadersAndStart(boolean insert) {
    appender.setInsertHeaders(insert);
    appender.start();
  }

  public void tearDown() throws SQLException {
    super.tearDown();
    context = null;
    appender = null;
    connectionSource = null;
  }

  public void testAppendAccessEvent() throws SQLException {
    setInsertHeadersAndStart(false);

    AccessEvent event = createAccessEvent();
    appender.append(event);
    
    Statement stmt = connectionSource.getConnection().createStatement();
    ResultSet rs = null;
    rs = stmt.executeQuery("SELECT * FROM access_event");
    if (rs.next()) {
      assertEquals(event.getTimeStamp(), rs.getLong(1));
      assertEquals(event.getRequestURI(), rs.getString(2));
      assertEquals(event.getRequestURL(), rs.getString(3));
      assertEquals(event.getRemoteHost(), rs.getString(4));
      assertEquals(event.getRemoteUser(), rs.getString(5));
      assertEquals(event.getRemoteAddr(), rs.getString(6));
      assertEquals(event.getProtocol(), rs.getString(7));
      assertEquals(event.getMethod(), rs.getString(8));
      assertEquals(event.getServerName(), rs.getString(9));
      assertEquals(event.getRequestContent(), rs.getString(10));
    } else {
      fail("No row was inserted in the database");
    }

    rs.close();
    stmt.close();
  }
  
  
  public void testCheckNoHeadersAreInserted() throws Exception {
    setInsertHeadersAndStart(false);
    
    AccessEvent event = createAccessEvent();
    appender.append(event);
    StatusPrinter.print(context.getStatusManager());
    
    //Check that no headers were inserted
    Statement stmt = connectionSource.getConnection().createStatement();
    ResultSet rs = null;
    rs = stmt.executeQuery("SELECT * FROM access_event_header");
    
    assertFalse(rs.next());
    rs.close();
    stmt.close();
  }

  public void testAppendHeaders() throws SQLException {   
    setInsertHeadersAndStart(true);
    
    AccessEvent event = createAccessEvent();
    appender.append(event);

    Statement stmt = connectionSource.getConnection().createStatement();
    ResultSet rs = null;
    rs = stmt.executeQuery("SELECT * FROM access_event_header");
    String key;
    String value;
    if (!rs.next()) {
      fail("There should be results to this query");
    } else {
      key = rs.getString(2);
      value = rs.getString(3);
      assertNotNull(key);
      assertNotNull(value);
      assertEquals(event.getRequestHeader(key), value);
      rs.next();
      key = rs.getString(2);
      value = rs.getString(3);
      assertNotNull(key);
      assertNotNull(value);
      assertEquals(event.getRequestHeader(key), value);
    }
    if (rs.next()) {
      fail("There should be no more rows available");
    }

    rs.close();
    stmt.close();
  }

  public void testAppendMultipleEvents() throws SQLException {
    for (int i = 0; i < 10; i++) {
      AccessEvent event = createAccessEvent();
      appender.append(event);
    }

    Statement stmt = connectionSource.getConnection().createStatement();
    ResultSet rs = null;
    rs = stmt.executeQuery("SELECT * FROM access_event");
    int count = 0;
    while (rs.next()) {
      count++;
    }
    assertEquals(10, count);

    rs.close();
    stmt.close();
  }

  private AccessEvent createAccessEvent() {
    DummyRequest request = new DummyRequest();
    DummyResponse response = new DummyResponse();
    DummyServerAdapter adapter = new DummyServerAdapter(request, response);

    AccessEvent ae = new AccessEvent(request, response, adapter);
    return ae;
  }
}
