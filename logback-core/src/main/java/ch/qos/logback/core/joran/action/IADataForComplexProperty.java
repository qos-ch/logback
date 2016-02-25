/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.util.PropertySetter;
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
