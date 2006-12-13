package org.apache.log4j;

public class MDC {

  public static void put(String key, String value) {
    ch.qos.logback.classic.MDC.put(key, value);
  }
  
  public static void put(String key, Object value) {
    if (value != null) {
      put(key, value.toString());
    } else {
      put(key, null);
    }
  }
  
  public static Object get(String key) {
    return ch.qos.logback.classic.MDC.get(key);
  }
  
  public static void remove(String key) {
    ch.qos.logback.classic.MDC.remove(key);
  }
  
  public static void clear() {
    ch.qos.logback.classic.MDC.clear();
  }
}
