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

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.PropertyModel;

/**
 * This class serves to build a model for properties which are to the ANT
 * &lt;property&gt; task which add/set properties of a given object.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class PropertyAction extends BaseModelAction {

    static final String RESOURCE_ATTRIBUTE = "resource";


    @Override
    protected boolean validPreconditions(InterpretationContext interpretationContext, String localName, Attributes attributes) {
        if ("substitutionProperty".equals(localName)) {
            addWarn("[substitutionProperty] element has been deprecated. Please use the [property] element instead.");
        }
        return true;
    }

    @Override
    protected Model buildCurrentModel(InterpretationContext interpretationContext, String name, Attributes attributes) {
        PropertyModel propertyModel = new PropertyModel();
        propertyModel.setName(attributes.getValue(NAME_ATTRIBUTE));
        propertyModel.setValue(attributes.getValue(VALUE_ATTRIBUTE));
        propertyModel.setScopeStr(attributes.getValue(SCOPE_ATTRIBUTE));
        propertyModel.setFile(attributes.getValue(FILE_ATTRIBUTE));
        propertyModel.setResource(attributes.getValue(RESOURCE_ATTRIBUTE));
        return propertyModel;
    }


}
