package ch.qos.logback.access.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import org.hsqldb.Server;

public abstract class DBAppenderTestBase extends TestCase {

  public static final String DRIVER_CLASS = "org.hsqldb.jdbcDriver";
  String serverProps;
  String url;
  String user = "sa";
  String password = "";
  Server server;
  boolean isNetwork = true;

  public DBAppenderTestBase(String name) {
    super(name);
  }

  protected void setUp() throws SQLException {
    if (isNetwork) {
      if (url == null) {
        url = "jdbc:hsqldb:hsql://localhost/test";
      }

      server = new Server();

      server.setDatabaseName(0, "test");
      server.setDatabasePath(0, "mem:test;sql.enforce_strict_size=true");
      server.setLogWriter(null);
      server.setErrWriter(null);
      server.setTrace(false);
      server.setSilent(true);
      server.start();
    } else {
      if (url == null) {
        url = "jdbc:hsqldb:file:test;sql.enforce_strict_size=true";
      }
    }

    try {
      Class.forName(DRIVER_CLASS);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(this + ".setUp() error: " + e.getMessage());
    }

    createTables();
  }

  protected void tearDown() throws SQLException {
    dropTables();
    if (isNetwork) {
      server.stop();
      server = null;
    }
  }

  Connection newConnection() throws SQLException {
    return DriverManager.getConnection(url, user, password);
  }

  void createTables() throws SQLException {
    Connection conn = newConnection();
    StringBuffer buf = new StringBuffer();
    buf.append("CREATE TABLE access_event (");
    buf.append("timestmp BIGINT NOT NULL,");
    buf.append("requestURI VARCHAR(254),");
    buf.append("requestURL VARCHAR(254),");
    buf.append("remoteHost VARCHAR(254),");
    buf.append("remoteUser VARCHAR(254),");
    buf.append("remoteAddr VARCHAR(254),");
    buf.append("protocol VARCHAR(254),");
    buf.append("method VARCHAR(254),");
    buf.append("serverName VARCHAR(254),");
    buf.append("postContent VARCHAR(254),");
    buf.append("event_id INT NOT NULL IDENTITY);");
    query(conn, buf.toString());

    buf = new StringBuffer();
    buf.append("CREATE TABLE access_event_header (");
    buf.append("event_id INT NOT NULL,");
    buf.append("header_key  VARCHAR(254) NOT NULL,");
    buf.append("header_value LONGVARCHAR,");
    buf.append("PRIMARY KEY(event_id, header_key),");
    buf.append("FOREIGN KEY (event_id) REFERENCES access_event(event_id));");
    query(conn, buf.toString());
  }

  void dropTables() throws SQLException {
    Connection conn = newConnection();
    
    StringBuffer buf = new StringBuffer();
    buf.append("DROP TABLE access_event_header IF EXISTS;");
    query(conn, buf.toString());
    
    buf = new StringBuffer();
    buf.append("DROP TABLE access_event IF EXISTS;");
    query(conn, buf.toString());
  }

  void query(Connection conn, String expression) throws SQLException {
    Statement st = null;
    st = conn.createStatement();

    int i = st.executeUpdate(expression);
    if (i == -1) {
      System.out.println("db error : " + expression);
    }

    st.close();
  }
}
