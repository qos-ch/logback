package ch.qos.logback.access.pattern.helpers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.access.spi.ServerAdapter;

/**
 * A test-only implementation of the {@link ServerAdapter} interface.
 *
 * @author S&eacute;bastien Pennec
 */
public class DummyValuesAdapter implements ServerAdapter {

  DummyRequest request;
  DummyResponse response;
  
  public DummyValuesAdapter(HttpServletRequest request, HttpServletResponse response) {
    this.request = (DummyRequest)request;
    this.response = (DummyResponse)response;
  }
  
  public long getContentLength() {
    return 123L;
  }

  public String getResponseHeader(String key) {
    return response.getHeader(key);
  }

  public int getStatusCode() {
    return 1;
  }

  public List<String> getResponseHeaderNameList() {
    return null;
  }

}
