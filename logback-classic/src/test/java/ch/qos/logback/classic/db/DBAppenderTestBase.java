package ch.qos.logback.classic.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import junit.framework.TestCase;

import org.hsqldb.Server;
import org.hsqldb.ServerConstants;
import org.hsqldb.jdbcDriver;

public abstract class DBAppenderTestBase extends TestCase {

  public static final String DRIVER_CLASS = "org.hsqldb.jdbcDriver";
  // String serverProps;
  String url = null;
  String user = "sa";
  String password = "";
  Server server;

  // boolean isNetwork = true;
  HsqlMode mode = HsqlMode.MEM;

  public DBAppenderTestBase(String name) {
    super(name);
  }

  protected void setUp() throws SQLException {

    switch (mode) {
    case NET:
      url = "jdbc:hsqldb:hsql://localhost:4808/test";
      break;
    case MEM:
      url = "jdbc:hsqldb:mem:test;sql.enforce_strict_size=true";
      server = new Server();
      server.setDatabaseName(0, "test");
      server.setDatabasePath(0, url);
      server.setLogWriter(new PrintWriter(System.out));
      server.setErrWriter(new PrintWriter(System.out));
      server.setTrace(false);
      server.setSilent(false);
      server.start();

      break;
    case FILE:
      url = "jdbc:hsqldb:file:test;sql.enforce_strict_size=true";
      break;

    }

    // try {
    // Class.forName(DRIVER_CLASS);
    // } catch (Exception e) {
    // e.printStackTrace();
    // System.out.println(this + ".setUp() error: " + e.getMessage());
    // }
    // Thread.yield();
    System.out.println(server.getState());

    int waitCount = 0;
    while (server.getState() != ServerConstants.SERVER_STATE_ONLINE
        && waitCount < 5) {
      try {
        waitCount++;
        Thread.sleep(1);
      } catch (InterruptedException e) {
      }
    }
    createTables();
  }

  protected void tearDown() throws SQLException {
    dropTables();

    if (mode == HsqlMode.MEM) {
      server.stop();
      server = null;
    }
  }

  Connection newConnection() throws SQLException {
    jdbcDriver driver = new jdbcDriver();
    Properties props = new Properties();
    props.setProperty("user", user);
    props.setProperty("password", password);
    return driver.connect(url, props);

    // return DriverManager.getConnection(url, user, password);
  }

  void createTables() throws SQLException {
    Connection conn = newConnection();
    assertNotNull(conn);
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

  public enum HsqlMode {
    MEM, FILE, NET;
  }
}
