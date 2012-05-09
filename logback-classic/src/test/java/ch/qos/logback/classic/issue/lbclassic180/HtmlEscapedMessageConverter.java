package ch.qos.logback.classic.issue.lbclassic180;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.helpers.Transform;

public class HtmlEscapedMessageConverter extends ClassicConverter {

  public String convert(ILoggingEvent event) {
    return Transform.escapeTags(event.getFormattedMessage());
  }
}
