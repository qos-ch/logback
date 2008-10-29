package ch.qos.logback.access.jetty;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.testUtil.NotifyingListAppender;
import ch.qos.logback.core.ConsoleAppender;

public class JettyFixture {
  RequestLogImpl requestLogImpl;

  public static final int PORT = 1234;
  
  public JettyFixture(RequestLogImpl impl) {
    requestLogImpl = impl;
  }

  public String getName() {
    return "Jetty Test Setup";
  }

  Server server;
  String url = "http://localhost:" + PORT + "/";

  public void start() throws Exception {
    // System.out.println("into setUp");

    server = new Server();
    Connector connector = new SelectChannelConnector();
    connector.setPort(PORT);
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
    
    Thread.yield();
  }

  public void stop() throws Exception {
    // System.out.println("into tearDown");
    server.stop();
    Thread.sleep(1000);
    server = null;
    requestLogImpl = null;
  }

  private void buildContext() {

    NotifyingListAppender appender = new NotifyingListAppender();
    appender.setContext(requestLogImpl);
    appender.setName("list");
    appender.start();

    ConsoleAppender<AccessEvent> console = new ConsoleAppender<AccessEvent>();
    console.setContext(requestLogImpl);
    console.setName("console");
    PatternLayout layout = new PatternLayout();
    layout.setContext(requestLogImpl);
    layout.setPattern("%date %server %clientHost");
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