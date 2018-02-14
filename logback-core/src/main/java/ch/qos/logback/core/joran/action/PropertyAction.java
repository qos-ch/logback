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
 * This class serves as a base for other actions, which similar to the ANT
 * &lt;property&gt; task which add/set properties of a given object.
 * 
 * This action sets new substitution properties in the logging context by name,
 * value pair, or adds all the properties passed in "file" or "resource"
 * attribute.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class PropertyAction extends Action {

    static final String RESOURCE_ATTRIBUTE = "resource";

    Object parent;
    PropertyModel propertyModel;

    /**
     * Set a new property for the execution context by name, value pair, or adds
     * all the properties found in the given file.
     * 
     */
    public void begin(InterpretationContext interpretationContext, String localName, Attributes attributes) {
        parent = null;

        if ("substitutionProperty".equals(localName)) {
            addWarn("[substitutionProperty] element has been deprecated. Please use the [property] element instead.");
        }

        parent = interpretationContext.peekObject();

        propertyModel = new PropertyModel();

        propertyModel.setName(attributes.getValue(NAME_ATTRIBUTE));
        propertyModel.setValue(attributes.getValue(VALUE_ATTRIBUTE));
        propertyModel.setScopeStr(attributes.getValue(SCOPE_ATTRIBUTE));
        propertyModel.setFile(attributes.getValue(FILE_ATTRIBUTE));
        propertyModel.setResource(attributes.getValue(RESOURCE_ATTRIBUTE));

        interpretationContext.pushObject(propertyModel);
    }

    public void end(InterpretationContext interpretationContext, String name) {
        Object o = interpretationContext.peekObject();

        if (o != propertyModel) {
            addWarn("The object at the of the stack is not the model [" + propertyModel.getTag() + "] pushed earlier.");
        } else {
            if (parent instanceof Model) {
                Model parentModel = (Model) parent;
                parentModel.addSubModel(propertyModel);
            }
            interpretationContext.popObject();
        }
    }

    public void finish(InterpretationContext ec) {
    }
}
