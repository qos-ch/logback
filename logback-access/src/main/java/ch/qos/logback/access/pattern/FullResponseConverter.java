package ch.qos.logback.access.pattern;

import java.util.List;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.Layout;

public class FullResponseConverter extends AccessConverter {

  @Override
  public String convert(AccessEvent ae) {
    StringBuffer buf = new StringBuffer();
    
    buf.append("HTTP/1.1 ");
    buf.append(ae.getStatusCode());
    buf.append(" NA");
    buf.append(Layout.LINE_SEP);
    
    List<String> hnList = ae.getResponseHeaderNameList();
    for(String headerName: hnList) {
      buf.append(headerName);
      buf.append(": ");
      buf.append(ae.getResponseHeader(headerName));
      buf.append(Layout.LINE_SEP);
    }
    buf.append(Layout.LINE_SEP);
    buf.append(ae.getResponseContent());
    buf.append(Layout.LINE_SEP);
    return buf.toString();
  }

}
