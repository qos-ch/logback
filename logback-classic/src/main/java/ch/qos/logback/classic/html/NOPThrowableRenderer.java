package ch.qos.logback.classic.html;

import ch.qos.logback.classic.spi.LoggingEvent;

public class NOPThrowableRenderer implements IThrowableRenderer {

  public void render(StringBuffer sbuf, LoggingEvent event) {
    return;
  }

}
