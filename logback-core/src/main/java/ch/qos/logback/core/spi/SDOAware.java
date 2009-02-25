package ch.qos.logback.core.spi;

import java.io.Serializable;

/**
 * 
 * SDOAware objects can return SDO substitutes of themselves. SDO stands for
 * Serializable Data Object which are usually read-only versions of the objects
 * they substitute for.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public interface SDOAware {
  /**
   * Return an SDO (Serializable Data Object) of this instance.
   * 
   * @return an SDO
   */
  Serializable getSDO();
}
