package ch.qos.logback.classic.spi;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Interface for attaching {@link TurboFilter} instances to container object.
 * 
 * <p>The container will implement this interface if it wishes that TurboFilter
 * can be attached to it.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public interface XTurboFilterAttachable {

  /**
   * Add a filter at the end of .
   * 
   * @param newFilter
   *                the filer to add
   */
  public boolean add(TurboFilter newFilter);

  /**
   * Add a filter at a specified position
   * 
   * @param index
   *                the position at which to insert the new element
   * @param newFilter
   *                the filter to add
   */
  public void add(int index, TurboFilter newFilter);

  /**
   * Clear the chain.
   */
  public void clear();

  /**
   * Remove the specified element from the filter chain.
   * 
   * @param turboFilter
   * @return true if the chain contained the specified turbo filter
   */
  public boolean remove(Object o);

  /**
   * Remove a turbo filter at a specified position in this list.
   * 
   * @param index
   *                of the filter to be removed
   * @return the filter previously at the specified position
   */
  public TurboFilter remove(int index);

  /**
   * Returns the number of elements in this chain.
   * 
   * @return the number of filters in this chain.
   */
  public int size();

  /**
   * Loop through the filters in the chain. As soon as a filter decides on
   * ACCEPT or DENY, then that value is returned. If all of the filters return
   * NEUTRAL, then NEUTRAL is returned.
   */
  public FilterReply getTurboFilterChainDecision(Marker marker, Logger logger,
      Level level, String format, Object[] params, Throwable t);

}
