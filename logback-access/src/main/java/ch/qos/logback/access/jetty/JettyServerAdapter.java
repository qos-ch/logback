package ch.qos.logback.access.jetty;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.jetty.HttpFields;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;

import ch.qos.logback.access.spi.ServerAdapter;

/**
 * A jetty specific implementation of the {@link ServerAdapter} interface.
 * 
 * @author S&eacute;bastien Pennec
 * @author Ceki Gulcu
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

  public Map<String, String> buildResponseHeaderMap() {
    Map<String, String> responseHeaderMap = new HashMap<String, String>();
    HttpFields httpFields = response.getHttpFields();
    Enumeration e = httpFields.getFieldNames();
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      String value = response.getHeader(key);
      responseHeaderMap.put(key, value);
    }
    return responseHeaderMap;
  }
  
}
