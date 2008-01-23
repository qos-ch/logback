package ch.qos.logback.access.pattern;

import java.util.Arrays;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.util.OptionHelper;

public class RequestParameterConverter extends AccessConverter {

  String key;

  public void start() {
    key = getFirstOption();
    if (OptionHelper.isEmpty(key)) {
      addWarn("Missing key for the request parameter");
    } else {
      super.start();
    }
  }

  public String convert(AccessEvent accessEvent) {
    if (!isStarted()) {
      return "INACTIVE_REQUEST_PARAM_CONV";
    }

    String[] paramArray = accessEvent.getRequestParameter(key);
    if (paramArray.length == 1) {
      return paramArray[0];
    } else {
      // for an array string {"a", "b"} named 'sa', Array.toString(sa) returns the string 
      // "[a, b]".
      return Arrays.toString(paramArray);
    }
  }

}
