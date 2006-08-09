package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

public class RemoteUserConverter extends AccessConverter {

  
  protected String convert(AccessEvent accessEvent) {    
    
    String user = accessEvent.getRemoteUser();
    if(user == null) {
      return AccessEvent.NA;
    } else {
      return user;
    }
  }

}
