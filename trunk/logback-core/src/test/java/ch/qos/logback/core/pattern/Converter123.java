package ch.qos.logback.core.pattern;

import ch.qos.logback.core.pattern.DynamicConverter;

public class Converter123 extends DynamicConverter {

  public String convert(Object event) {
    return "123";
  }

}
