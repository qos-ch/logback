package ch.qos.logback.access.jetty;

import java.util.HashMap;
import java.util.Map;

public class RequestLogMapper {
  
  private static Map<String, RequestLogImpl> requestLogMap = new HashMap<String, RequestLogImpl>();
  
  public static void addRequestLog(RequestLogImpl requestLogImpl) {
    requestLogMap.put(requestLogImpl.getName(), requestLogImpl);
  }
  
  public static RequestLogImpl get(String key) {
    return requestLogMap.get(key);
  }

}
