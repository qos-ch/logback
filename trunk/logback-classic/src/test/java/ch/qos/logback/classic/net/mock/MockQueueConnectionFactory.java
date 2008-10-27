package ch.qos.logback.classic.net.mock;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;

public class MockQueueConnectionFactory implements QueueConnectionFactory {

  MockQueueConnection cnx = new MockQueueConnection();
  
  public QueueConnection createQueueConnection() throws JMSException {
    return cnx;
  }

  public QueueConnection createQueueConnection(String user, String pass) throws JMSException {
    
    return cnx;
  }

  public Connection createConnection() throws JMSException {
    return null;
  }

  public Connection createConnection(String arg0, String arg1) throws JMSException {
    return null;
  }

}
