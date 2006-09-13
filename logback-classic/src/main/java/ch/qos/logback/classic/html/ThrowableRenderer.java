package ch.qos.logback.classic.html;

import static ch.qos.logback.core.Layout.LINE_SEP;
import ch.qos.logback.classic.helpers.Transform;
import ch.qos.logback.classic.spi.LoggingEvent;

public class ThrowableRenderer {
  
  static final String TRACE_PREFIX = "<br />&nbsp;&nbsp;&nbsp;&nbsp;";
  
  Throwable throwable;
  
  public ThrowableRenderer() {
    
  }
  
  public void setThrowable(Throwable t) {
    this.throwable = t;
  }

  public boolean newLineRequired(LoggingEvent event) {
    return event.getThrowableInformation() != null;
  }
  
  public void render(StringBuffer sbuf, String[] s) {
    if (s != null) {
      int len = s.length;
      if (len == 0) {
        return;
      }
      sbuf.append(Transform.escapeTags(s[0]));
      sbuf.append(LINE_SEP);
      for (int i = 1; i < len; i++) {
        sbuf.append(TRACE_PREFIX);
        sbuf.append(Transform.escapeTags(s[i]));
        sbuf.append(LINE_SEP);
      }
    }    
  }
  
  public void render(StringBuffer sbuf, LoggingEvent event) {
    render(sbuf, event.getThrowableInformation().getThrowableStrRep());
  }
}
