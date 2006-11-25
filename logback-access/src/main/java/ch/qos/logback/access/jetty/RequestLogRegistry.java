package ch.qos.logback.access.jetty;

import java.util.HashMap;
import java.util.Map;

public class RequestLogRegistry {
  
  private static Map<String, RequestLogImpl> requestLogRegistry = new HashMap<String, RequestLogImpl>();
  
  public static void register(RequestLogImpl requestLogImpl) {
    requestLogRegistry.put(requestLogImpl.getName(), requestLogImpl);
  }
  
  public static RequestLogImpl get(String key) {
    return requestLogRegistry.get(key);
  }

}
