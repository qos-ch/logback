package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

public class RemoteUserConverter extends AccessConverter {

  
  public String convert(AccessEvent accessEvent) {    
    
    String user = accessEvent.getRemoteUser();
    if(user == null) {
      return AccessEvent.NA;
    } else {
      return user;
    }
  }

}
