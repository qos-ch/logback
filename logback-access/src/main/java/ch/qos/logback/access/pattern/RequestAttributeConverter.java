package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.util.OptionHelper;


public class RequestAttributeConverter extends AccessConverter {

  String key;

  public void start() {
    key = getFirstOption();
    if (OptionHelper.isEmpty(key)) {
      addWarn("Missing key for the request attribute");
    } else {
      super.start();
    }
  }

  public String convert(AccessEvent accessEvent) {
    if (!isStarted()) {
      return "INACTIVE_REQUEST_ATTRIB_CONV";
    }

    return accessEvent.getAttribute(key);
  }

}
