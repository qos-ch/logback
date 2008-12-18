package ch.qos.logback.classic.sift;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class AppenderFactory {

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

  Appender<LoggingEvent> buildAppender(Context context, String mdcKey,
      String mdcValue) throws JoranException {
    //HoardingContext hoardingContext = new HoardingContext(context, mdcKey,
    //    mdcValue);
    HoardingJoranConfigurator hjc = new HoardingJoranConfigurator(mdcKey, mdcValue);
    hjc.setContext(context);

    hjc.doConfigure(eventList);

    StatusPrinter.print(context);

    return hjc.getAppender();
  }

  public List<SaxEvent> getEventList() {
    return eventList;
  }

}
