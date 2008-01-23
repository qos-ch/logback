package ch.qos.logback.access.dummy;

import ch.qos.logback.access.spi.AccessEvent;

public class DummyAccessEventBuilder {

  
  static public AccessEvent buildNewAccessEvent() {
    DummyRequest request = new DummyRequest();
    DummyResponse response = new DummyResponse();
    DummyServerAdapter adapter = new DummyServerAdapter(request, response);
    
    AccessEvent ae = new AccessEvent(request, response, adapter);
    return ae;
  }
  
}
