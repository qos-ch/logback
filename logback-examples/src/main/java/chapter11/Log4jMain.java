/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
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
