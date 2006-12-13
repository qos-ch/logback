package ch.qos.logback.core.filter;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * Users should extend this class to implement customized event filtering in
 * addition to the build-in filtering rules. It is suggested that you first use
 * and understand the built-in rules before rushing to write your own custom
 * filters.
 * <p>
 * For more informations about filters, please refer to the online manual at
 * http://logback.qos.ch/manual/filters.html
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public abstract class Filter extends ContextAwareBase implements LifeCycle {

  /**
   * Points to the next filter in the filter chain.
   */
  private Filter next;
  
  private String name;
  
  boolean start = false;

  public void start() {
    this.start = true;
  }

  public boolean isStarted() {
    return this.start;
  }

  public void stop() {
    this.start = false;
  }

  /**
   * If the decision is <code>{@link #DENY}</code>, then the event will be
   * dropped. If the decision is <code>{@link #NEUTRAL}</code>, then the next
   * filter, if any, will be invoked. If the decision is
   * <code>{@link #ACCEPT}</code> then the event will be logged without
   * consulting with other filters in the chain.
   * 
   * @param event
   *          The event to decide upon.
   */
  public abstract FilterReply decide(Object event);

  /**
   * Set the next filter pointer.
   */
  public void setNext(Filter next) {
    this.next = next;
  }

  /**
   * Return the pointer to the next filter;
   */
  public Filter getNext() {
    return next;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
