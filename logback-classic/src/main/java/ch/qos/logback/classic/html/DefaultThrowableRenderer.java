package ch.qos.logback.classic.html;

import static ch.qos.logback.core.Layout.LINE_SEP;
import ch.qos.logback.classic.helpers.Transform;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableInformation;
import ch.qos.logback.core.html.IThrowableRenderer;

public class DefaultThrowableRenderer implements IThrowableRenderer {
  
  static final String TRACE_PREFIX = "<br />&nbsp;&nbsp;&nbsp;&nbsp;";
  
  Throwable throwable;
  
  public DefaultThrowableRenderer() {
    
  }
  
  public void setThrowable(Throwable t) {
    this.throwable = t;
  }
  
  public void render(StringBuilder sbuf, String[] s) {
    if (s != null) {
      int len = s.length;
      if (len == 0) {
        return;
      }
      sbuf.append("<tr><td class=\"Exception\" colspan=\"6\">");
      sbuf.append(Transform.escapeTags(s[0]));
      sbuf.append(LINE_SEP);
      for (int i = 1; i < len; i++) {
        sbuf.append(TRACE_PREFIX);
        sbuf.append(Transform.escapeTags(s[i]));
        sbuf.append(LINE_SEP);
      }
      sbuf.append("</td></tr>");
    }
  }
  
  public void render(StringBuilder sbuf, Object eventObject) {
    LoggingEvent event = (LoggingEvent)eventObject;
    ThrowableInformation ti = event.getThrowableInformation();
    if (ti != null) {
      render(sbuf, ti.getThrowableStrRep());
    }
  }
}
