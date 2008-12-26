package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.spi.PropertySetter;
import ch.qos.logback.core.util.AggregationType;

/**
 * Lump together several fields for use by {@link NestedComplexPropertyIA}.
 * 
 * @author Ceki
 */
public class IADataForComplexProperty {
  final PropertySetter parentBean;
  final AggregationType aggregationType;
  final String complexPropertyName;
  private Object nestedComplexProperty;
  boolean inError;

  public IADataForComplexProperty(PropertySetter parentBean, AggregationType aggregationType, String complexPropertyName) {
    this.parentBean = parentBean;
    this.aggregationType = aggregationType;
    this.complexPropertyName = complexPropertyName;
  }

  public AggregationType getAggregationType() {
    return aggregationType;
  }

  public Object getNestedComplexProperty() {
    return nestedComplexProperty;
  }

  public String getComplexPropertyName() {
    return complexPropertyName;
  }

  public void setNestedComplexProperty(Object nestedComplexProperty) {
    this.nestedComplexProperty = nestedComplexProperty;
  }
  
  
}
