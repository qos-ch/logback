package ch.qos.logback.classic.spi;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.filter.ClassicFilter;

/**
 * Interface for attaching ClassicFilter instances to objects.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public interface ClassicFilterAttachable {

  /**
   * Add a filter.
   */
  public void addFilter(ClassicFilter newFilter);

  /**
   * Get first filter in the chain.
   */
  public ClassicFilter getFirstFilter();

  public void clearAllFilters();

  /**
   * Loop through the filters in the chain. As soon as a filter decides on
   * ACCEPT or DENY, then that value is returned. If all of the filters return
   * NEUTRAL, then NEUTRAL is returned.
   */
  public int getFilterChainDecision(Marker marker, Logger logger,
      Level level, String format, Object[] params, Throwable t);

}
