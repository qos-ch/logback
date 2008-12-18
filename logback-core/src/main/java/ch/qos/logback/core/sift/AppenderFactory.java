package ch.qos.logback.core.sift;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.JoranException;

public abstract class AppenderFactory<E, K> {

  final List<SaxEvent> eventList;
  Context context;
  
  AppenderFactory(Context context, List<SaxEvent> eventList) {
    this.context = context;
    this.eventList = new ArrayList<SaxEvent>(eventList);
    removeHoardElement();

  }

  void removeHoardElement() {
    eventList.remove(0);
    eventList.remove(eventList.size() - 1);
    System.out.println(eventList);
  }

  
  abstract SiftingJoranConfigurator<E> getSiftingJoranConfigurator(K k);
  
  Appender<E> buildAppender(Context context, K k) throws JoranException {
    SiftingJoranConfigurator<E> sjc = getSiftingJoranConfigurator(k);
    sjc.setContext(context);
    sjc.doConfigure(eventList);
    return sjc.getAppender();
  }

  public List<SaxEvent> getEventList() {
    return eventList;
  }

}
