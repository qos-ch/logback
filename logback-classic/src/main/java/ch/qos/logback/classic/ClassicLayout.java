package ch.qos.logback.classic;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Layout;


public interface ClassicLayout extends Layout<LoggingEvent> {

  /**
   * Transform an event of type {@link LoggingEvent) and return it as a String after
   * appropriate formatting. 
   * 
   * <p>In addition, the contract of this interface demands that the {@link #doLayout(Object)} 
   * method be implemented in terms of this method (taking in LoggingEvent).
   * 
   * <p>All layouts in the Classic module must implement this interface.
   * 
   * @param event
   *          The event to format
   * @return the event formatted as a String
   */
  String doLayout(LoggingEvent event);

}
