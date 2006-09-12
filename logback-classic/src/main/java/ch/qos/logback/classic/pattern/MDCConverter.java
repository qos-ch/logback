package ch.qos.logback.classic.pattern;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
      StringBuffer buf = new StringBuffer("{");
      Set<String> keys = mdcPropertyMap.keySet();
      Iterator it = keys.iterator();
      String tmpKey;
      String tmpValue;
      while (it.hasNext()) {
        tmpKey = (String)it.next();
        tmpValue = (String)mdcPropertyMap.get(tmpKey);
        //format: {testeKey=testValue, testKey2=testValue2}
        buf.append(tmpKey).append('=').append(tmpValue).append(", ");
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
