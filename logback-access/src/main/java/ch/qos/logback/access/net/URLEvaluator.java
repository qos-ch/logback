package ch.qos.logback.access.net;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

public class URLEvaluator extends ContextAwareBase implements EventEvaluator, LifeCycle {

  boolean started;
  String name;
  private List<String> URLList = new ArrayList<String>();

  public URLEvaluator() {
  }

  public void addURL(String url) {
    URLList.add(url);
  }

  public void start() {
    if (URLList.size() == 0) {
      addWarn("No URL was given to URLEvaluator");
    } else {
      started = true;
    }
  }
    
  public boolean evaluate(Object eventObject) throws NullPointerException, EvaluationException {
    AccessEvent event = (AccessEvent)eventObject;
    String url = event.getRequestURL();
    for(String expected:URLList) {
      if (url.contains(expected)) {
        return true;
      }
    }
    return false;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isStarted() {
    return started;
  }

  public void stop() {
    started = false;
  }
}
