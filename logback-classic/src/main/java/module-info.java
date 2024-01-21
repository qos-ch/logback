import ch.qos.logback.classic.spi.Configurator;

module ch.qos.logback.classic {
  // requires static means optional
  requires static java.management;

  // used by the optional ContextJNDISelector component
  requires static java.naming;

  // used by the optional LevelChangePropagator component
  requires static java.logging;

  // used by the optional ContextJNDISelector, MDCInsertingServletFilter among other components
  requires static jakarta.servlet;

  requires static jakarta.mail;

  requires org.slf4j;

  requires ch.qos.logback.core;
  uses ch.qos.logback.classic.spi.Configurator;

  exports ch.qos.logback.classic;
  exports ch.qos.logback.classic.model;
}

