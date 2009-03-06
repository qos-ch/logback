package ch.qos.logback.classic.corpus;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.IThrowableProxy;

public class LogStatement {

  final String loggerName;
  final MessageArgumentTuple mat;
  final Level level;
  final IThrowableProxy throwableProxy;

  public LogStatement(String loggerName, Level level, MessageArgumentTuple mat,
      IThrowableProxy tp) {
    this.loggerName = loggerName;
    this.level = level;
    this.mat = mat;
    this.throwableProxy = tp;
  }

}
