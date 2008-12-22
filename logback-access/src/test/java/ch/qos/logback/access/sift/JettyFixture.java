package ch.qos.logback.access.sift;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.util.ByteArrayISO8859Writer;

import ch.qos.logback.access.jetty.JettyFixtureBase;
import ch.qos.logback.access.jetty.RequestLogImpl;

public class JettyFixture extends JettyFixtureBase {

  Handler handler = new BasicHandler();

  public JettyFixture(RequestLogImpl impl, int port) {
    super(impl, port);
  }

  @Override
  protected void buildContext() {
    requestLogImpl.start();
  }

  @Override
  protected Handler getHandler() {
    return handler;
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
}
