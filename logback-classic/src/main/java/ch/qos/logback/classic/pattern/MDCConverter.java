package ch.qos.logback.classic.pattern;

import java.util.Map;

import ch.qos.logback.classic.spi.LoggingEvent;

public class MDCConverter extends ClassicConverter {

  String key;
  
  public MDCConverter() {
  }

  @Override
  public void start() {
    key = getFirstOption();
    super.start();
  }
  
  @Override
  public void stop() {
    key = null;
    super.stop();
  }
  
  @Override
  public String convert(Object event) {
    LoggingEvent loggingEvent = (LoggingEvent) event;
    Map<String, String> mdcPropertyMap = loggingEvent.getMDCPropertyMap();
    if (mdcPropertyMap != null) {
      return loggingEvent.getMDCPropertyMap().get(key);
    } else {
      return "";
    }
  }
}
