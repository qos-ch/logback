
package ch.qos.logback.core.joran.action;

import java.util.Properties;

import ch.qos.logback.core.joran.spi.InterpretationContext;


/**
 * @author Ceki Gulcu
 */
public class RepositoryPropertyAction extends PropertyAction {
  
  /**
   * Add all the properties found in the argument named 'props' to an ExecutionContext.
   */
  public void setProperties(InterpretationContext ec, Properties props) {
    // TODO : test this method
    for(Object o: props.keySet()) {
      String key = (String) o;
      this.context.setProperty(key, props.getProperty(key));
    }
  }
  
  public void setProperty(InterpretationContext ec, String key, String value) {
    this.context.setProperty(key, value);
  }
}
