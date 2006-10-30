package ch.qos.logback.core.appender;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

public class NOPAppender extends AppenderBase {

  @Override
  protected void append(Object eventObject) {
   }

  public Layout getLayout() {
    return null;
  }

  public void setLayout(Layout layout) {
  }

}
