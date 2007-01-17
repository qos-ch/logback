package ch.qos.logback.access.pattern;

import java.util.Enumeration;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.Layout;

/**
 * This class is tied to the <code>requestContent</code> conversion word.
 * <p>
 * It has been removed from the {@link ch.qos.logback.access.PatternLayout} since
 * it needs further testing before wide use.
 * <p>
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class FullRequestConverter extends AccessConverter {

  @Override
  public String convert(AccessEvent ae) {
    StringBuffer buf = new StringBuffer();
    buf.append(ae.getRequestURL());
    buf.append(Layout.LINE_SEP);
    
    Enumeration headerNames = ae.getRequestHeaderNames();
    while(headerNames.hasMoreElements()) {
      String name = (String) headerNames.nextElement();
      buf.append(name);
      buf.append(": ");
      buf.append(ae.getRequestHeader(name));
      buf.append(Layout.LINE_SEP);
    }
    buf.append(Layout.LINE_SEP);
    buf.append(ae.getRequestContent());
    return buf.toString();
  }

}
