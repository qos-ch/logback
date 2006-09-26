package ch.qos.logback.classic.db;

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

  public DBAppenderTestBase(String name, String url, boolean isNetwork) {

    super(name);

    this.isNetwork = isNetwork;
    this.url = url;
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
    buf.append("CREATE TABLE logging_event (");
    buf.append("timestmp BIGINT NOT NULL,");
    buf.append("formatted_message LONGVARCHAR NOT NULL,");
    buf.append("logger_name VARCHAR(256) NOT NULL,");
    buf.append("level_string VARCHAR(256) NOT NULL,");
    buf.append("thread_name VARCHAR(256),");
    buf.append("reference_flag SMALLINT,");
    buf.append("caller_filename VARCHAR(256), ");
    buf.append("caller_class VARCHAR(256), ");
    buf.append("caller_method VARCHAR(256), ");
    buf.append("caller_line CHAR(4), ");
    buf.append("event_id INT NOT NULL IDENTITY);");
    query(conn, buf.toString());

    buf = new StringBuffer();
    buf.append("CREATE TABLE logging_event_property (");
    buf.append("event_id INT NOT NULL,");
    buf.append("mapped_key  VARCHAR(254) NOT NULL,");
    buf.append("mapped_value LONGVARCHAR,");
    buf.append("PRIMARY KEY(event_id, mapped_key),");
    buf.append("FOREIGN KEY (event_id) REFERENCES logging_event(event_id));");
    query(conn, buf.toString());

    buf = new StringBuffer();
    buf.append("CREATE TABLE logging_event_exception (");
    buf.append("event_id INT NOT NULL,");
    buf.append("i SMALLINT NOT NULL,");
    buf.append("trace_line VARCHAR(256) NOT NULL,");
    buf.append("PRIMARY KEY(event_id, i),");
    buf.append("FOREIGN KEY (event_id) REFERENCES logging_event(event_id));");
    query(conn, buf.toString());
  }
  
  void dropTables() throws SQLException {
    Connection conn = newConnection();
    StringBuffer buf = new StringBuffer();
    buf.append("DROP TABLE logging_event_exception IF EXISTS;");
    query(conn, buf.toString());
    
    buf = new StringBuffer();
    buf.append("DROP TABLE logging_event_property IF EXISTS;");
    query(conn, buf.toString());
    
    buf = new StringBuffer();
    buf.append("DROP TABLE logging_event IF EXISTS;");
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
