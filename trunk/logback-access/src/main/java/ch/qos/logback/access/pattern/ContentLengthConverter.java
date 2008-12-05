package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

public class ContentLengthConverter extends AccessConverter {

  public String convert(AccessEvent accessEvent) {
    long len = accessEvent.getContentLength();
    if(len == AccessEvent.SENTINEL) {
      return AccessEvent.NA;
    } else {
    return Long.toString(len);
    } 
  }

}
