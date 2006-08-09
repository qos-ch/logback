package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.util.OptionHelper;


public class ResponseHeaderConverter extends AccessConverter {

  String key;

  public void start() {
    key = getFirstOption();
    if (OptionHelper.isEmpty(key)) {
      addWarn("Missing key for the response header");
    } else {
      super.start();
    }
  }

  protected String convert(AccessEvent accessEvent) {
    if(!isStarted()) {
      return "INACTIVE_REPONSE_HEADER_CONV";
    }
    
    return null;
    
//    HttpServletResponse response = accessEvent.getHttpResponse();
//
//    Object value = null; // = response.getHeader(key);
//    if (value == null) {
//      return AccessConverter.NA;
//    } else {
//      return value.toString();
//    }
  }

}
