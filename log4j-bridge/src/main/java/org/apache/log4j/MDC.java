package org.apache.log4j;

public class MDC {

  public static void put(String key, String value) {
    org.slf4j.MDC.put(key, value);
  }
  
  public static void put(String key, Object value) {
    if (value != null) {
      put(key, value.toString());
    } else {
      put(key, null);
    }
  }
  
  public static Object get(String key) {
    return org.slf4j.MDC.get(key);
  }
  
  public static void remove(String key) {
    org.slf4j.MDC.remove(key);
  }
  
  public static void clear() {
    org.slf4j.MDC.clear();
  }
}
