module ch.qos.logback.classic { 
  requires org.slf4j;
  requires static java.management;
  requires static javax.servlet.api;
  requires ch.qos.logback.core;
  uses ch.qos.logback.classic.spi.Configurator;
  provides org.slf4j.spi.SLF4JServiceProvider with ch.qos.logback.classic.spi.LogbackServiceProvider;
  
  
  exports ch.qos.logback.classic;
  exports ch.qos.logback.classic.boolex;
  exports ch.qos.logback.classic.db;
  exports ch.qos.logback.classic.db.names;
  exports ch.qos.logback.classic.encoder;
  exports ch.qos.logback.classic.filter;
  //exports ch.qos.logback.classic.gaffer;
  exports ch.qos.logback.classic.helpers;
  exports ch.qos.logback.classic.html;
  exports ch.qos.logback.classic.jmx;
  exports ch.qos.logback.classic.joran;
  exports ch.qos.logback.classic.joran.action;
  exports ch.qos.logback.classic.jul;
  exports ch.qos.logback.classic.layout;
  exports ch.qos.logback.classic.log4j;
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

