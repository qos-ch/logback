package ch.qos.logback.classic.net;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

public class JMSQueueAppenderTestApp {
  
  public static void main(String[] args) {
    Logger logger = (Logger)LoggerFactory.getLogger(JMSTopicAppenderTestApp.class);
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.shutdownAndReset();
    
    JMSQueueAppender appender = new JMSQueueAppender();
    appender.setContext(lc);
    appender.setName("jmsQueue");
    appender.setInitialContextFactoryName("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
    //appender.setPassword("");
    appender.setProviderURL("tcp://localhost:61616");
    //appender.setSecurityCredentials("");
    //appender.setSecurityPrincipalName("");
    appender.setQueueBindingName("MyQueue");
    appender.setQueueConnectionFactoryBindingName("ConnectionFactory");
    //appender.setURLPkgPrefixes("");
    //appender.setUserName("");
    
    appender.start();
    
    logger.addAppender(appender);
    
    for (int i = 0; i < 10; i++) {
      logger.debug("** Hello world. n=" + i);
    }
    
    StatusPrinter.print(lc.getStatusManager());
  }

}
