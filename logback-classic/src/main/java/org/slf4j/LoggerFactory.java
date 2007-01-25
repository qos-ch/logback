/*
 * Copyright (c) 2004-2005 SLF4J.ORG
 * Copyright (c) 2004-2005 QOS.ch
 *
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute, and/or sell copies of  the Software, and to permit persons
 * to whom  the Software is furnished  to do so, provided  that the above
 * copyright notice(s) and this permission notice appear in all copies of
 * the  Software and  that both  the above  copyright notice(s)  and this
 * permission notice appear in supporting documentation.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR  A PARTICULAR PURPOSE AND NONINFRINGEMENT
 * OF  THIRD PARTY  RIGHTS. IN  NO EVENT  SHALL THE  COPYRIGHT  HOLDER OR
 * HOLDERS  INCLUDED IN  THIS  NOTICE BE  LIABLE  FOR ANY  CLAIM, OR  ANY
 * SPECIAL INDIRECT  OR CONSEQUENTIAL DAMAGES, OR  ANY DAMAGES WHATSOEVER
 * RESULTING FROM LOSS  OF USE, DATA OR PROFITS, WHETHER  IN AN ACTION OF
 * CONTRACT, NEGLIGENCE  OR OTHER TORTIOUS  ACTION, ARISING OUT OF  OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 * Except as  contained in  this notice, the  name of a  copyright holder
 * shall not be used in advertising or otherwise to promote the sale, use
 * or other dealings in this Software without prior written authorization
 * of the copyright holder.
 *
 */
package org.slf4j;

import org.slf4j.impl.Util;

import ch.qos.logback.classic.ClassicGlobal;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextJNDISelector;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.selector.DefaultContextSelector;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.util.OptionHelper;

/**
 * The <code>LoggerFactory</code> is a utility class producing Loggers for
 * various logging APIs, most notably for NLOG4J and JDK 1.4 logging. Other
 * implementations such as {@link org.slf4j.impl.NOPLogger NOPLogger} and
 * {@link org.slf4j.impl.SimpleLogger SimpleLogger} are also supported.
 * 
 * <p>
 * <code>LoggerFactory</code> is essentially a wrapper around an
 * {@link ILoggerFactory} instance bound with <code>LoggerFactory</code> at
 * compile time.
 * 
 * <p>
 * Please note that all methods in <code>LoggerFactory</code> are static.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public final class LoggerFactory {

  static LoggerContext defaultLoggerContext;
  
  private static ContextSelector contextSelector;

  // private constructor prevents instantiation
  private LoggerFactory() {
  }

  static {
    try {
      //let's configure a default context
      defaultLoggerContext = new LoggerContext();
      defaultLoggerContext.setName("default");
      ContextInitializer.autoConfig(defaultLoggerContext);
      
      //See if a special context selector is needed
      String contextSelectorStr = OptionHelper.getSystemProperty(ClassicGlobal.LOGBACK_CONTEXT_SELECTOR, null);
      if (contextSelectorStr == null) {
        contextSelector = new DefaultContextSelector(defaultLoggerContext);
      } else if (contextSelectorStr.equals("JNDI")) {
        //if jndi is specified, let's use the appropriate class
        contextSelector = new ContextJNDISelector(defaultLoggerContext);
      }
    } catch (Exception e) {
      // we should never get here
      Util.reportFailure("Failed to instantiate logger [" + LoggerContext.class
          + "]", e);
    }
  }

  /**
   * Return a logger named according to the name parameter using the statically
   * bound {@link ILoggerFactory} instance.
   * 
   * @param name
   *          The name of the logger.
   * @return logger
   */
  public static Logger getLogger(String name) {
    return contextSelector.getLoggerContext().getLogger(name);
  }

  /**
   * Return a logger named corresponding to the class passed as parameter, using
   * the statically bound {@link ILoggerFactory} instance.
   * 
   * @param clazz
   *          the returned logger will be named after clazz
   * @return logger
   */
  public static Logger getLogger(Class clazz) {
    return contextSelector.getLoggerContext().getLogger(clazz.getName());
  }

  /**
   * Return the {@link ILoggerFactory} instance in use.
   * 
   * <p>
   * ILoggerFactory instance is bound with this class at compile time.
   * 
   * @return the ILoggerFactory instance in use
   */
  public static ILoggerFactory getILoggerFactory() {
    return contextSelector.getLoggerContext();
  }
  
  /**
   * Return the {@link ContextSelector} instance in use.
   * 
   * @return the ContextSelector instance in use
   */
  public static ContextSelector getContextSelector() {
    return contextSelector;
  }
}
