package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.util.PropertySetter;

public class ImplicitActionData {
  PropertySetter parentBean;
  String propertyName;
  Object nestedComponent;
  int containmentType;
  boolean inError;

  ImplicitActionData(PropertySetter parentBean, int containmentType) {
    this.parentBean = parentBean;
    this.containmentType = containmentType;
  }
}
