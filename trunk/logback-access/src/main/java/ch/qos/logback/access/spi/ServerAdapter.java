package ch.qos.logback.access.spi;

import java.util.Map;

/**
 * An interface to access server-specific methods from
 * the server-independent AccessEvent.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public interface ServerAdapter {

  long getContentLength();
  int getStatusCode();
//  String getResponseHeader(String key);
//  List<String> getResponseHeaderNameList();
//  Map<String, String> getResponseHeaderMap();
  
  Map<String, String> buildResponseHeaderMap();
}
