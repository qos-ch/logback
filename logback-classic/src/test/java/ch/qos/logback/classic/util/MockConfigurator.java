package ch.qos.logback.classic.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.spi.ContextAwareBase;

public class MockConfigurator extends ContextAwareBase implements Configurator {

  static LoggerContext context = null;

  public void configure(LoggerContext loggerContext) {
    context = loggerContext;
  }
}
