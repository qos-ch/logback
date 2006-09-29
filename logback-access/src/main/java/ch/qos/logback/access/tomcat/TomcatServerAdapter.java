package ch.qos.logback.access.tomcat;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

import ch.qos.logback.access.spi.ServerAdapter;

/**
 * A tomcat specific implementation of the {@link ServerAdapter} interface.
 *
 * @author S&eacute;bastien Pennec
 */
public class TomcatServerAdapter implements ServerAdapter {
  
  Request request;
  Response response;

  public TomcatServerAdapter(Request tomcatRequest, Response tomcatResponse) {
    this.request = tomcatRequest;
    this.response = tomcatResponse;
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
