package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

import ch.qos.logback.classic.LoggerContext;

/**
 * 
 * The binding of {@link LoggerFactory} class with an actual instance of 
 * {@link ILoggerFactory} is performed using information returned by this class. 
 * 
 * @author <a href="http://www.qos.ch/log4j/">Ceki G&uuml;lc&uuml;</a>
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

  /**
   * The unique instance of this class.
   */
  public static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
  private static final String loggerFactoryClassStr = LoggerContext.class.getName();

  /** The ILoggerFactory instance returned by the {@link #getLoggerFactory} method
   * should always be the same object
   */
  private final ILoggerFactory loggerFactory;
  
  private StaticLoggerBinder() {
  	LoggerContext lc = new LoggerContext();
  	 lc.setName("default");
    loggerFactory = lc;
  }
  
  public ILoggerFactory getLoggerFactory() {
    return loggerFactory;
  }
  
  public String getLoggerFactoryClassStr() {
    return loggerFactoryClassStr;
  }   
}
