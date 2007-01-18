package ch.qos.logback.classic.pattern;

import java.util.Iterator;

import org.slf4j.Marker;

import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * Return the event's marker value(s).
 * 
 * @author S&eacute;bastien Pennec
 */
public class MarkerConverter extends ClassicConverter {
  
  private static String EMPTY = "";
  private static String OPEN = "[ ";
  private static String CLOSE = " ]";
  private static String SEP = ", ";
  
  public String convert(LoggingEvent le) {
    Marker marker = le.getMarker();
    if (marker == null) {
      return EMPTY;
    }
      
    if (!marker.hasChildren()) {
      return marker.getName();
    }
    
    Iterator it = marker.iterator();
    Marker child;
    StringBuffer sb = new StringBuffer(marker.getName());
    sb.append(' ').append(OPEN);
    while(it.hasNext()) {
      child = (Marker)it.next();
      sb.append(child.getName());
      if (it.hasNext()) {
        sb.append(SEP);
      }
    }
    sb.append(CLOSE);
      
    return sb.toString();  
  }

}
