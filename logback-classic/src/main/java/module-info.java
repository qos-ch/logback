module ch.qos.logback.classic { 
  requires org.slf4j;
  requires ch.qos.logback.core;
  requires static groovy;
  requires static java.management;
  
  provides org.slf4j.spi.SLF4JServiceProvider with ch.qos.logback.classic.provider.LogbackServiceProvider;
}

