package ch.qos.logback.classic.spi;

import java.util.Map;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.spi.SDOAware;

public interface ILoggingEvent extends SDOAware {

  public String getThreadName();

  public Level getLevel();

  public String getMessage();
  
  public LoggerRemoteView getLoggerRemoteView();
  
  public String getFormattedMessage();

  public Object[] getArgumentArray();

  public ThrowableProxy getThrowableProxy();

  public CallerData[] getCallerData();

  public Marker getMarker();

  public Map<String, String> getMDCPropertyMap();

  public long getTimeStamp();
  
  public long getContextBirthTime();
  
  public void prepareForDeferredProcessing();

}
