package ch.qos.logback.classic.corpus;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.IThrowableProxy;

public class LogStatement {

  final String loggerName;
  final MessageItem messagerItem;
  final Level level;
  final IThrowableProxy throwableProxy;

  public LogStatement(String loggerName, Level level, MessageItem messagerItem,
      IThrowableProxy tp) {
    this.loggerName = loggerName;
    this.level = level;
    this.messagerItem = messagerItem;
    this.throwableProxy = tp;
  }

  public String getLoggerName() {
    return loggerName;
  }

  public MessageItem getMessagerItem() {
    return messagerItem;
  }

}
