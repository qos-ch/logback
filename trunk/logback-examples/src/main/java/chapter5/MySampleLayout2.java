package chapter5;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;

public class MySampleLayout2 extends LayoutBase<LoggingEvent> {

  String prefix = null;
  boolean printThreadName = true;

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public void setPrintThreadName(boolean printThreadName) {
    this.printThreadName = printThreadName;
  }

  public String doLayout(LoggingEvent event) {
    StringBuffer sbuf = new StringBuffer(128);
    if (prefix != null) {
      sbuf.append(prefix + ": ");
    }
    sbuf.append(event.getTimeStamp() - LoggingEvent.getStartTime());
    sbuf.append(" ");
    sbuf.append(event.getLevel());
    if (printThreadName) {
      sbuf.append(" [");
      sbuf.append(event.getThreadName());
      sbuf.append("] ");
    } else {
      sbuf.append(" ");
    }
    sbuf.append(event.getLoggerRemoteView().getName());
    sbuf.append(" - ");
    sbuf.append(event.getFormattedMessage());
    sbuf.append(CoreConstants.LINE_SEPARATOR);
    return sbuf.toString();
  }
}