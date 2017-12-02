module ch.qos.logback.classic { 

    requires org.slf4j;
    requires static java.logging;
    requires static java.management;
    
    
    requires ch.qos.logback.core;
    
    provides org.slf4j.spi.SLF4JServiceProvider with ch.qos.logback.classic.provider.LogbackServiceProvider;

}