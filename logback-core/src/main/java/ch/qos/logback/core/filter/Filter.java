package ch.qos.logback.core.filter;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * Users should extend this class to implement customized event filtering in
 * addition to the build-in filtering rules. It is suggested that you first use
 * and understand the built-in rules before rushing to write your own custom
 * filters.
 * <p>
 * This abstract class assumes and also imposes that filters be organized in a
 * linear chain. The {@link #decide decide(Object)} method of each filter is
 * called sequentially, in the order of their addition to the chain.
 * <p>
 * The decide() method must return one of the FilterReplies {@link #DENY},
 * {@link #NEUTRAL} or {@link #ACCEPT}.
 * <p>
 * If the value DENY is returned, then the log event is dropped immediately
 * without consulting with the remaining filters.
 * 
 * <p>
 * If the value NEUTRAL is returned, then the next filter in the chain is
 * consulted. If there are no more filters in the chain, then the log event is
 * logged. Thus, in the presence of no filters, the default behaviour is to log
 * all logging events.
 * 
 * <p>
 * If the value ACCEPT is returned, then the log event is logged without
 * consulting the remaining filters.
 * 
 * <p>
 * The philosophy of logback filters are largely inspired from Linux ipchains.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public abstract class Filter extends ContextAwareBase implements LifeCycle {

  /**
   * The event must be dropped immediately without consulting with the remaining
   * filters, if any, in the chain.
   */
  //public static final int DENY = -1;

  /**
   * This filter is neutral with respect to the event. The remaining filters, if
   * any, should be consulted for a final decision.
   */
  //public static final int NEUTRAL = 0;

  /**
   * The event must be logged immediately without consulting with the remaining
   * filters, if any, in the chain.
   */
  //public static final int ACCEPT = 1;

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
