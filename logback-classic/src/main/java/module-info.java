module ch.qos.logback.classic { 
  requires org.slf4j;
  requires static java.management;
  requires static javax.servlet.api;
  requires ch.qos.logback.core;
  uses ch.qos.logback.classic.spi.Configurator;
  provides org.slf4j.spi.SLF4JServiceProvider with ch.qos.logback.classic.spi.LogbackServiceProvider;
  
  
  exports ch.qos.logback.classic;
  exports ch.qos.logback.classic.spi;
}

