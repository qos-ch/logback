package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class ThreadIdConverter extends ClassicConverter {

  @Override
  public String convert(ILoggingEvent event) {
    return event.getThreadId();
  }

}
