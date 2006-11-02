package ch.qos.logback.classic.spi;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.filter.FilterReply;

/**
 * Interface for attaching ClassicFilter instances to objects.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public interface TurboFilterAttachable {

  /**
   * Add a filter.
   */
  public void addTurboFilter(TurboFilter newFilter);

  /**
   * Get first filter in the chain.
   */
  public TurboFilter getFirstTurboFilter();

  public void clearAllTurboFilters();

  /**
   * Loop through the filters in the chain. As soon as a filter decides on
   * ACCEPT or DENY, then that value is returned. If all of the filters return
   * NEUTRAL, then NEUTRAL is returned.
   */
  public FilterReply getTurboFilterChainDecision(Marker marker, Logger logger,
      Level level, String format, Object[] params, Throwable t);

}
