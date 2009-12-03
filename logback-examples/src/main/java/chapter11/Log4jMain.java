/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package chapter11;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * A minimal application making use of log4j and TrivialLog4jAppender.
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class Log4jMain {

  static Logger logger = Logger.getLogger(Log4jMain.class);

  public static void main(String[] args) {
    PropertyConfigurator.configure("src/main/java/chapter11/log4jTrivial.properties");
    logger.debug("Hello world");
  }

}
