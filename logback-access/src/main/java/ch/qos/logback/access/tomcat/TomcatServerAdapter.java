package ch.qos.logback.access.tomcat;

import java.util.HashMap;
import java.util.Map;

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
    return response.getContentLength();
  }

  public int getStatusCode() {
    return response.getStatus();
  }

  
  public Map<String, String> buildResponseHeaderMap() {
    Map<String, String> responseHeaderMap = new HashMap<String, String>();
    for (String key : response.getHeaderNames()) {
      String value = response.getHeader(key);
      responseHeaderMap.put(key, value);
    }
    return responseHeaderMap;
  }
  


}
