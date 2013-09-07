/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.selector;

import java.util.List;

import ch.qos.logback.classic.LoggerContext;

/**
 * An interface that provides access to different contexts.
 * 
 * It is used by the LoggerFactory to access the context
 * it will use to retrieve loggers.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public interface ContextSelector {

  LoggerContext getLoggerContext();
  
  LoggerContext getLoggerContext(String name);
  
  LoggerContext getDefaultLoggerContext();
  
  LoggerContext detachLoggerContext(String loggerContextName);
  
  List<String> getContextNames();
}
