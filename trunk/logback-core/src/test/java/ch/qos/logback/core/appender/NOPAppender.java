package ch.qos.logback.core.appender;

import ch.qos.logback.core.AppenderBase;

final public class NOPAppender<E> extends AppenderBase<E> {

  @Override
  final protected void append(E eventObject) {
  }
}
 