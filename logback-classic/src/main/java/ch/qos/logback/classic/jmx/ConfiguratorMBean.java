package ch.qos.logback.classic.jmx;

import java.net.URL;
import java.util.List;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.Status;

public interface ConfiguratorMBean {
  
  public void reload();
  
  public void reload(String fileName) throws JoranException;
  
  public void reload(URL url) throws JoranException;
  
  public void setLoggerLevel(String loggerName, String levelStr);
  
  public String getLoggerLevel(String loggerName);
  
  public String getLoggerEffectiveLevel(String loggerName);

  public List<Logger> getLoggerList();
  
  public List<Status> getStatuses();
}
