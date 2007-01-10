package chapter5;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.LayoutBase;

public class MySampleLayout extends LayoutBase<LoggingEvent>  {

  public String doLayout(LoggingEvent event) {
    StringBuffer sbuf = new StringBuffer(128);
    sbuf.append(event.getTimeStamp() - LoggingEvent.getStartTime());
    sbuf.append(" ");
    sbuf.append(event.getLevel());
    sbuf.append(" [");
    sbuf.append(event.getThreadName());
    sbuf.append("] ");
    sbuf.append(event.getLoggerRemoteView().getName());
    sbuf.append(" - ");
    sbuf.append(event.getFormattedMessage());
    sbuf.append(LINE_SEP);
    return sbuf.toString();
  }
}