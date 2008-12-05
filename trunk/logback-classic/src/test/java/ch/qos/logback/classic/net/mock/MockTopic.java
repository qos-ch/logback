package ch.qos.logback.classic.net.mock;

import javax.jms.JMSException;
import javax.jms.Topic;

public class MockTopic implements Topic {

  String name;
  
  public MockTopic(String name) {
    this.name = name;
  }
  
  public String getTopicName() throws JMSException {
    return name;
  }

}
