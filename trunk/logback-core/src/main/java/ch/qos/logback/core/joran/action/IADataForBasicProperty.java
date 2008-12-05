package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.spi.PropertySetter;
import ch.qos.logback.core.util.AggregationType;

/**
 * Lump together several fields for use by {@link NestedBasicPropertyIA}.
 * 
 * @author Ceki Gulcu
 */
class IADataForBasicProperty {
  final PropertySetter parentBean;
  final AggregationType aggregationType;
  final String propertyName;
  boolean inError;

  IADataForBasicProperty(PropertySetter parentBean, AggregationType aggregationType, String propertyName) {
    this.parentBean = parentBean;
    this.aggregationType = aggregationType;
    this.propertyName = propertyName;
  }
}
