package ch.qos.logback.core.pattern;

import ch.qos.logback.core.pattern.DynamicConverter;

public class ExceptionalConverter extends DynamicConverter {
  
  public String convert(Object event) {
    if(!isStarted()) {
      throw new IllegalStateException("this converter must be started before use");
    }
    return "";
  }

}
