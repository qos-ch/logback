package ch.qos.logback.access.jetty;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.util.ByteArrayISO8859Writer;

import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.appender.ListAppender;

public class JettyTestSetup extends TestSetup {

  RequestLogImpl requestLogImpl;

  public JettyTestSetup(Test suite, RequestLogImpl impl) {
    super(suite);
    requestLogImpl = impl;
  }
  
  public String getName() {
    return "Jetty Test Setup";
  }

  Server server;
  String url = "http://localhost:8080/";

  public void setUp() throws Exception {
    //System.out.println("into setUp");
    super.setUp();

    server = new Server();
    Connector connector = new SelectChannelConnector();
    connector.setPort(8080);
    server.setConnectors(new Connector[] { connector });

    ContextHandler context = new ContextHandler();
    context.setContextPath("/");
    context.setResourceBase(".");
    context.setClassLoader(Thread.currentThread().getContextClassLoader());
    server.addHandler(context);

    RequestLogHandler requestLogHandler = new RequestLogHandler();
    buildContext();
    requestLogHandler.setRequestLog(requestLogImpl);
    server.addHandler(requestLogHandler);

    Handler handler = new BasicHandler();
    context.addHandler(handler);

    server.start();
  }

  public void tearDown() throws Exception {
    //System.out.println("into tearDown");
    super.tearDown();
    server.stop();
    Thread.sleep(1000);
    server = null;
    requestLogImpl = null;
  }

  private void buildContext() {

    ListAppender appender = new ListAppender();
    appender.setContext(requestLogImpl);
    appender.setName("list");
    appender.start();

    ConsoleAppender console = new ConsoleAppender();
    console.setContext(requestLogImpl);
    console.setName("console");
    PatternLayout layout = new PatternLayout();
    layout.setContext(requestLogImpl);
    layout.setPattern("%date %server %clientHost %post");
    console.setLayout(layout);
    layout.start();
    console.start();

    requestLogImpl.addAppender(appender);
    requestLogImpl.addAppender(console);
  }

}

class BasicHandler extends AbstractHandler {
  public void handle(String target, HttpServletRequest request,
      HttpServletResponse response, int dispatch) throws IOException,
      ServletException {

    // String requestContent = Util.readToString(request.getInputStream());
    // System.out.println("request content: " + requestContent);

    OutputStream out = response.getOutputStream();
    ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer();
    writer.write("hello world");
    writer.flush();
    response.setContentLength(writer.size());
    writer.writeTo(out);
    out.flush();

    Request base_request = (request instanceof Request) ? (Request) request
        : HttpConnection.getCurrentConnection().getRequest();
    base_request.setHandled(true);

  }
}