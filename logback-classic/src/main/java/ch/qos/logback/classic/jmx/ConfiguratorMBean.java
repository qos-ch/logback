package ch.qos.logback.classic.jmx;

import java.net.URL;

import ch.qos.logback.core.joran.spi.JoranException;

public interface ConfiguratorMBean {
  
  public void reload();
  
  public void reload(String fileName) throws JoranException;
  
  public void reload(URL url) throws JoranException;
  
  public void setLoggerLevel(String loggerName, String levelStr);
  
  public String getLoggerLevel(String loggerName);
  
  public String getLoggerEffectiveLevel(String loggerName);

}
