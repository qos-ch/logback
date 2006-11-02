package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.util.ContainmentType;
import ch.qos.logback.core.util.PropertySetter;

public class ImplicitActionData {
  PropertySetter parentBean;
  String propertyName;
  Object nestedComponent;
  ContainmentType containmentType;
  boolean inError;

  ImplicitActionData(PropertySetter parentBean, ContainmentType containmentType) {
    this.parentBean = parentBean;
    this.containmentType = containmentType;
  }
}
