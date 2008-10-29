package ch.qos.logback.access.jetty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.Util;
import ch.qos.logback.access.testUtil.NotifyingListAppender;

public class JettyBasicTest {

  static RequestLogImpl requestLogImpl;
  static JettyFixture fixture;

  @BeforeClass
  static public void startServer() throws Exception {
    // System.out.println("*** JettyBasicTest.startServer called");
    requestLogImpl = new RequestLogImpl();
    JettyFixture fixture = new JettyFixture(requestLogImpl);
    fixture.start();
  }

  @AfterClass
  static public void stopServer() throws Exception {
    // System.out.println("*** JettyBasicTest.stopServer called");
    if (fixture != null) {
      fixture.stop();
    }
  }

  @Test
  public void getRequest() throws Exception {
    URL url = new URL("http://localhost:" + JettyFixture.PORT + "/");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoInput(true);

    String result = Util.readToString(connection.getInputStream());

    assertEquals("hello world", result);

    NotifyingListAppender listAppender = (NotifyingListAppender) requestLogImpl
        .getAppender("list");
    listAppender.list.clear();
  }

  @Test
  public void eventGoesToAppenders() throws Exception {
    URL url = new URL("http://localhost:" + JettyFixture.PORT + "/");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoInput(true);

    String result = Util.readToString(connection.getInputStream());

    assertEquals("hello world", result);

    NotifyingListAppender listAppender = (NotifyingListAppender) requestLogImpl
        .getAppender("list");
    synchronized (listAppender) {
      listAppender.wait(100);
    }

    assertTrue(listAppender.list.size() > 0);
    AccessEvent event = (AccessEvent) listAppender.list.get(0);
    assertEquals("127.0.0.1", event.getRemoteHost());
    assertEquals("localhost", event.getServerName());
    listAppender.list.clear();
  }

  @Test
  public void postContentConverter() throws Exception {
    // System.out.println("into test");
    URL url = new URL("http://localhost:" + JettyFixture.PORT + "/");
    String msg = "test message";

    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    // this line is necessary to make the stream aware of when the message is
    // over.
    connection.setFixedLengthStreamingMode(msg.getBytes().length);
    ((HttpURLConnection) connection).setRequestMethod("POST");
    connection.setDoOutput(true);
    connection.setDoInput(true);
    connection.setUseCaches(false);
    connection.setRequestProperty("Content-Type", "text/plain");

    PrintWriter output = new PrintWriter(new OutputStreamWriter(connection
        .getOutputStream()));
    output.print(msg);
    output.flush();
    output.close();

    // StatusPrinter.print(requestLogImpl.getStatusManager());

    NotifyingListAppender listAppender = (NotifyingListAppender) requestLogImpl
        .getAppender("list");

    synchronized (listAppender) {
      listAppender.wait(100);
    }

    @SuppressWarnings("unused")
    AccessEvent event = (AccessEvent) listAppender.list.get(0);

    // we should test the contents of the requests
    // assertEquals(msg, event.getRequestContent());
  }
}
