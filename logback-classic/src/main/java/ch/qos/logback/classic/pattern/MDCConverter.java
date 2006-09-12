package ch.qos.logback.classic.pattern;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import ch.qos.logback.classic.spi.LoggingEvent;

public class MDCConverter extends ClassicConverter {

  String key;
  private static final String EMPTY_STRING = "";

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

    if (mdcPropertyMap == null) {
      return EMPTY_STRING;
    }

    if (key == null) {
      // if no key is specified, return all the
      // values present in the MDC, separated with a single space.
      StringBuffer buf = new StringBuffer();
      Collection<String> values = mdcPropertyMap.values();
      Iterator it = values.iterator();
      String value;
      while (it.hasNext()) {
        value = (String)it.next();
        buf.append(value).append(' ');
      }
      return buf.toString();
    }

    String value = loggingEvent.getMDCPropertyMap().get(key);
    if (value != null) {
      return value;
    } else {
      return EMPTY_STRING;
    }
  }
}
