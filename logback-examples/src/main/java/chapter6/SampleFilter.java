package chapter6;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class SampleFilter extends Filter {

  @Override
  public FilterReply decide(Object eventObject) {
    ILoggingEvent event = (ILoggingEvent)eventObject;
    if (event.getMessage() != null && event.getMessage().contains("sample")) {
      return FilterReply.ACCEPT;
    } else {
      return FilterReply.NEUTRAL;
    }
  }
}
