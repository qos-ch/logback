package ch.qos.logback.core.pattern;

import ch.qos.logback.core.pattern.DynamicConverter;

public class ConverterHello extends DynamicConverter {

  public String convert(Object event) {
    return "Hello";
  }

}
