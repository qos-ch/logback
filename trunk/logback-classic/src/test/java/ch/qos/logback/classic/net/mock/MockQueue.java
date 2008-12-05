package ch.qos.logback.classic.net.mock;

import javax.jms.JMSException;
import javax.jms.Queue;

public class MockQueue implements Queue {

  String name;
  
  public MockQueue(String name) {
    this.name = name;
  }
  
  public String getQueueName() throws JMSException {
    return name;
  }

}
