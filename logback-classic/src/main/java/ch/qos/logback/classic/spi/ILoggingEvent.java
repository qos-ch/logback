package ch.qos.logback.classic.spi;

import java.util.Map;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;

/**
 * The core interface in logback-classic.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public interface ILoggingEvent {

  public String getThreadName();
  public Level getLevel();
  public String getMessage();
  public Object[] getArgumentArray();
  public String getFormattedMessage();

  public LoggerRemoteView getLoggerRemoteView();

  public ThrowableProxy getThrowableProxy();

  public CallerData[] getCallerData();

  public Marker getMarker();

  public Map<String, String> getMDCPropertyMap();

  public long getTimeStamp();
  
  public long getContextBirthTime();
  
  public void prepareForDeferredProcessing();

}
