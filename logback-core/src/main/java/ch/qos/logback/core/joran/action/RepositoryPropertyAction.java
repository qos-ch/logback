
package ch.qos.logback.core.joran.action;

import java.util.Properties;

import ch.qos.logback.core.joran.spi.ExecutionContext;


/**
 * @author Ceki Gulcu
 */
public class RepositoryPropertyAction extends PropertyAction {
  
  /**
   * Add all the properties found in the argument named 'props' to an ExecutionContext.
   * 
   */
  @SuppressWarnings("unchecked")
  public void setProperties(ExecutionContext ec, Properties props) {
    this.context.getPropertyMap().putAll(props);
  }
  
  public void setProperty(ExecutionContext ec, String key, String value) {
    this.context.setProperty(key, value);
  }
}
