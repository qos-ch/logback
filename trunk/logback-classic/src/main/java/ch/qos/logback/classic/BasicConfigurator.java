/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.LoggingEvent;
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
    ConsoleAppender<LoggingEvent> ca = new ConsoleAppender<LoggingEvent>();
    ca.setContext(lc);
    ca.setName("console");
    PatternLayout pl = new PatternLayout();
    pl.setContext(lc);
    pl.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
    pl.start();

    ca.setLayout(pl);
    ca.start();
    Logger rootLogger = lc.getLogger(LoggerContext.ROOT_NAME);
    rootLogger.addAppender(ca);
  }

  public static void configureDefaultContext() {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    configure(lc);
  }
}
