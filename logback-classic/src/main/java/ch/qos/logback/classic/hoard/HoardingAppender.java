package ch.qos.logback.classic.hoard;

import java.util.Hashtable;
import java.util.Map;

import org.slf4j.MDC;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.joran.spi.JoranException;

public class HoardingAppender extends UnsynchronizedAppenderBase<LoggingEvent> {

  static String DEFAULT = "default";

  Map<String, Appender<LoggingEvent>> appenderMap = new Hashtable<String, Appender<LoggingEvent>>();

  String mdcKey;

  AppenderFactory appenderFactory;

  void setAppenderFactory(AppenderFactory appenderFactory) {
    this.appenderFactory = appenderFactory;
  }

  
  
  @Override
  protected void append(LoggingEvent loggingEvent) {
    String mdcValue = MDC.get(mdcKey);

    if (mdcValue == null) {
      mdcValue = DEFAULT;
    }

    Appender<LoggingEvent> appender = appenderMap.get(mdcValue);

    if (appender == null) {
      try {
        appender = appenderFactory.buildAppender(context, mdcKey, mdcValue);
      } catch (JoranException e) {
        addError("Failed to build appender for " + mdcKey + "=" + mdcValue, e);
        return;
      }
    }
    appender.doAppend(loggingEvent);
  }

  public String getMdcKey() {
    return mdcKey;
  }

  public void setMdcKey(String mdcKey) {
    this.mdcKey = mdcKey;
  }
  
  
}
