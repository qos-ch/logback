package ch.qos.logback.access.jetty;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;

import ch.qos.logback.access.spi.ServerAdapter;

/**
 * A jetty specific implementation of the {@link ServerAdapter} interface.
 *
 * @author S&eacute;bastien Pennec
 */
public class JettyServerAdapter implements ServerAdapter {
  
  Request request;
  Response response;

  public JettyServerAdapter(Request jettyRequest, Response jettyResponse) {
    this.request = jettyRequest;
    this.response = jettyResponse;
  }
  
  public long getContentLength() {
    return response.getContentCount();
  }

  public int getStatusCode() {
    return response.getStatus();
  }
  
  public String getResponseHeader(String key) {
    return response.getHeader(key);
  }

}
