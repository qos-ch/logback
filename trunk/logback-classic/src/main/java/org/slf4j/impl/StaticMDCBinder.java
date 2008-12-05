package org.slf4j.impl;

import org.slf4j.spi.MDCAdapter;


/**
 * This implementation is bound to {@link LogbackMDCAdapter}.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class StaticMDCBinder {

  
  /**
   * The unique instance of this class.
   */
  public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

  private StaticMDCBinder() {
  }
  
  /**
   * Currently this method always returns an instance of 
   * {@link StaticMDCBinder}.
   */
  public MDCAdapter getMDCA() {
     return new LogbackMDCAdapter();
  }
  
  public String  getMDCAdapterClassStr() {
    return LogbackMDCAdapter.class.getName();
  }
}
