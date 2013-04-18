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
package ch.qos.logback.classic;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;

/**
 * BasicConfigurator configures logback-classic by attaching a 
 * {@link ConsoleAppender} to the root logger. The console appender's layout 
 * is set to a {@link  PatternLayout} with the pattern 
 * "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n".
 * 
 * @author Ceki Gulcu
 */
public class BasicConfigurator {

  final static BasicConfigurator hiddenSingleton = new BasicConfigurator();
    
  private BasicConfigurator() {
  }
  
  public static void configure(LoggerContext lc) {
    StatusManager sm = lc.getStatusManager();
    if(sm != null)  {
     sm.add(new InfoStatus("Setting up default configuration.", lc));
    }
    ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<ILoggingEvent>();
    ca.setContext(lc);
    ca.setName("console");
    PatternLayoutEncoder pl = new PatternLayoutEncoder();
    pl.setContext(lc);
    pl.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
    pl.start();

    ca.setEncoder(pl);
    ca.start();
    Logger rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.addAppender(ca);
  }

  public static void configureDefaultContext() {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    configure(lc);
  }
}
