/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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
package chapters.architecture;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class SelectionRule {

  public static void main(String[] args) {
    // get a logger instance named "com.foo". Let us further assume that the
    // logger is of type  ch.qos.logback.classic.Logger so that we can
    // set its level
    ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("com.foo");
    //set its Level to INFO. The setLevel() method requires a logback logger
    logger.setLevel(Level.INFO);

    Logger barlogger = LoggerFactory.getLogger("com.foo.Bar");

    // This request is enabled, because WARN >= INFO
    logger.warn("Low fuel level.");

    // This request is disabled, because DEBUG < INFO.
    logger.debug("Starting search for nearest gas station.");

    // The logger instance barlogger, named "com.foo.Bar",
    // will inherit its level from the logger named
    // "com.foo" Thus, the following request is enabled
    // because INFO >= INFO.
    barlogger.info("Located nearest gas station.");

    // This request is disabled, because DEBUG < INFO.
    barlogger.debug("Exiting gas station search");

  }
}
