package ch.qos.logback.classic.net.mock;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

public class MockTopicConnectionFactory implements TopicConnectionFactory {

  MockTopicConnection cnx = new MockTopicConnection();
  
  public TopicConnection createTopicConnection() throws JMSException {
    return cnx;
  }

  public TopicConnection createTopicConnection(String user, String pass) throws JMSException {
    
    return cnx;
  }

  public Connection createConnection() throws JMSException {
    return null;
  }

  public Connection createConnection(String arg0, String arg1) throws JMSException {
    return null;
  }

}
