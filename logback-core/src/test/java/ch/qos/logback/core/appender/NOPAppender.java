package ch.qos.logback.core.appender;

import ch.qos.logback.core.AppenderBase;

public class NOPAppender<E> extends AppenderBase<E> {

  @Override
  protected void append(E eventObject) {
  }
}
 