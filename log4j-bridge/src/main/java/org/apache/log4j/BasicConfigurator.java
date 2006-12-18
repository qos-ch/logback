package org.apache.log4j;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

public class BasicConfigurator {

  static public void configure() {
    ch.qos.logback.classic.BasicConfigurator.configureDefaultContext();
  }

  public static void resetConfiguration() {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.shutdownAndReset();
  }
}
