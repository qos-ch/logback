package ch.qos.logback.access;


import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.Layout;


public interface AccessLayout extends Layout {

  /**
   * Transform an event of type {@link AccessEvent) and return it as a String after
   * appropriate formatting. 
   * 
   * <p>In addition, the contract of this interface demands that the {@link #doLayout(Object)} 
   * method be implemented in terms of this method (taking in AccessEvent).
   * 
   * <p>All layouts in the Access module must implement this interface.
   * 
   * @param event
   *          The event to format
   * @return the event formatted as a String
   */
  String doLayout(AccessEvent event);

}
