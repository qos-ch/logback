package ch.qos.logback.access.net;

import ch.qos.logback.access.boolex.JaninoEventEvaluator;

public class DefaultSMTPEvaluator extends JaninoEventEvaluator {
  
  private String url;
  
  public DefaultSMTPEvaluator() { 
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  @Override
  public void start() {
    setExpression("request.getRequestURL().toString().contains(\"" + url + "\")");
    super.start();
  }
}
