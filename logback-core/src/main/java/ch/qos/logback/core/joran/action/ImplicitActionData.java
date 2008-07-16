package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.util.AggregationType;
import ch.qos.logback.core.util.PropertySetter;

/**
 * ImplicitActionData is a data class aggregating several fields.
 * 
 * 
 * @author Ceki
 */
public class ImplicitActionData {
  PropertySetter parentBean;
  String propertyName;
  Object nestedComponent;
  AggregationType containmentType;
  boolean inError;

  ImplicitActionData(PropertySetter parentBean, AggregationType containmentType) {
    this.parentBean = parentBean;
    this.containmentType = containmentType;
  }
}
