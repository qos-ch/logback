package ch.qos.logback.classic.net;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

public class JMSTopicAppenderTestApp {
  
  public static void main(String[] args) {
    Logger logger = (Logger)LoggerFactory.getLogger(JMSTopicAppenderTestApp.class);
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.reset();
    
    JMSTopicAppender appender = new JMSTopicAppender();
    appender.setContext(lc);
    appender.setName("jmsTopic");
    appender.setInitialContextFactoryName("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
    //appender.setPassword("");
    appender.setProviderURL("tcp://localhost:61616");
    //appender.setSecurityCredentials("");
    //appender.setSecurityPrincipalName("");
    appender.setTopicBindingName("MyTopic");
    appender.setTopicConnectionFactoryBindingName("ConnectionFactory");
    //appender.setURLPkgPrefixes("");
    //appender.setUserName("");
    
    appender.start();
    logger.addAppender(appender);
    
    
    //JIT
    for (int i = 0; i < 10000; i++) {
      logger.debug("** Hello world. n=" + i);
    }
    
    long before = System.nanoTime();
    for (int i = 0; i < 10000; i++) {
      logger.debug("** Hello world. n=" + i);
    }
    long after = System.nanoTime();
    
    System.out.println("Time per logs for 10'000 logs: " + (after-before)/10000);
    
    StatusPrinter.print(lc.getStatusManager());
  }

}