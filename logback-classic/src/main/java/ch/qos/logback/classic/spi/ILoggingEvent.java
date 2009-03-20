package ch.qos.logback.classic.spi;

import java.util.Map;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;

/**
 * The core interface in logback-classic.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since 0.9.16
 */
public interface ILoggingEvent {

  public String getThreadName();

  public Level getLevel();

  public String getMessage();

  public Object[] getArgumentArray();

  public String getFormattedMessage();

  public String getLoggerName();

  public LoggerContextVO getLoggerContextVO();

  public IThrowableProxy getThrowableProxy();

  /**
   * Return caller data associated with this event. Note that calling
   * this event may trigger the computation of caller data.
   * 
   * @return the caller data associated with this event.
   * 
   * @see #hasCallerData()
   */
  public StackTraceElement[] getCallerData();

  /**
   * If this event has caller data, then true is returned. Otherwise the
   * returned value is null.
   * 
   * <p>Logback components wishing to use caller data if available without
   * causing it to be computed can invoke this method before invoking
   * {@link #getCallerData()}.
   * 
   * @return whether this event has caller data
   */
  public boolean hasCallerData();

  public Marker getMarker();

  public Map<String, String> getMDCPropertyMap();

  public long getTimeStamp();

  public void prepareForDeferredProcessing();

}
