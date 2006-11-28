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
    appender.start();
  }

  public void tearDown() throws SQLException {
    super.tearDown();
    context = null;
    appender = null;
    connectionSource = null;
  }

  public void testAppendAccessEvent() throws SQLException {
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
      assertEquals(event.getPostContent(), rs.getString(10));
    } else {
      fail("No row was inserted in the database");
    }

    rs.close();
    stmt.close();
  }

  public void testAppendHeaders() throws SQLException {    
    AccessEvent event = createAccessEvent();
    appender.append(event);

    Statement stmt = connectionSource.getConnection().createStatement();
    ResultSet rs = null;
    rs = stmt.executeQuery("SELECT * FROM access_event_header where event_id = 0");
    while (rs.next()) {
      assertEquals(event.getRequestHeader(rs.getString(2)), rs.getString(3));
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
