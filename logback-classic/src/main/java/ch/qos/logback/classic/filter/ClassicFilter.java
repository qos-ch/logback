package ch.qos.logback.classic.filter;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.filter.Filter;

/**
 * ClassicFilter is a specialized filter with a decide method that takes a bunch of parameters
 * instead of a single event object. The latter is cleaner but the latter is much more 
 * performant.
 * 
 * @author Ceki Gulcu
 */
public abstract class ClassicFilter extends Filter {

  /**
   * Points to the next filter in the filter chain.
   */
  private ClassicFilter classicNext;

  /**
   * Make a decision based on the multiple parameters passed as arguments.
   * The returned value should be one of <code>{@link #DENY}</code>, 
   * <code>{@link #NEUTRAL}</code>, or <code>{@link #ACCEPT}</code>.
  
   * @param marker
   * @param logger
   * @param level
   * @param format
   * @param params
   * @param t
   * @return
   */
  public abstract int decide(Marker marker, Logger logger,
      Level level, String format, Object[] params, Throwable t);

  public int decide(Object o) {
    throw new UnsupportedOperationException("decide(Object) not supported");
  }

  /**
   * Set the next filter pointer.
   */
  public void setNext(ClassicFilter next) {
    this.classicNext = next;
  }

  /**
   * Return the pointer to the next filter;
   */
  public ClassicFilter getNext() {
    return classicNext;
  }

}
