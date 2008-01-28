package ch.qos.logback.access.dummy;

import java.util.Map;

import ch.qos.logback.access.spi.ServerAdapter;

public class DummyServerAdapter implements ServerAdapter {

  DummyRequest request;
  DummyResponse response;
  
  public DummyServerAdapter(DummyRequest dummyRequest, DummyResponse dummyResponse) {
    this.request = dummyRequest;
    this.response = dummyResponse;
  }
  
  public long getContentLength() {
    return response.getContentCount();
  }

  public int getStatusCode() {
    return response.getStatus();
  }
  
  public Map<String, String> buildResponseHeaderMap() {
    return response.headerMap;
  }

}
