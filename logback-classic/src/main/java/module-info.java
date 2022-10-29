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
  provides org.slf4j.spi.SLF4JServiceProvider with ch.qos.logback.classic.spi.LogbackServiceProvider;

  provides ch.qos.logback.classic.spi.Configurator with ch.qos.logback.classic.util.DefaultJoranConfigurator;

  exports ch.qos.logback.classic;
  exports ch.qos.logback.classic.boolex;
  exports ch.qos.logback.classic.encoder;
  exports ch.qos.logback.classic.filter;
  exports ch.qos.logback.classic.helpers;
  exports ch.qos.logback.classic.html;
  exports ch.qos.logback.classic.joran;
  exports ch.qos.logback.classic.joran.action;
  exports ch.qos.logback.classic.jul;
  exports ch.qos.logback.classic.layout;
  exports ch.qos.logback.classic.log4j;
  exports ch.qos.logback.classic.model;
  exports ch.qos.logback.classic.net;
  exports ch.qos.logback.classic.net.server;
  exports ch.qos.logback.classic.pattern;
  exports ch.qos.logback.classic.pattern.color;
  exports ch.qos.logback.classic.selector;
  exports ch.qos.logback.classic.selector.servlet;
  exports ch.qos.logback.classic.servlet;
  exports ch.qos.logback.classic.sift;
  exports ch.qos.logback.classic.spi;
  exports ch.qos.logback.classic.turbo;
  exports ch.qos.logback.classic.util;
}

