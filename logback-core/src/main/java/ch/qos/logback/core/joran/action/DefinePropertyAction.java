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
import ch.qos.logback.core.model.DefineModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.util.OptionHelper;

/**
 * Creates {@link DefineModel} instance and populate its name, className and scope.
 * 
 * @author Aleksey Didik
 * @author Ceki G&uml;lc&uml;
 */
public class DefinePropertyAction extends BaseModelAction {

    @Override
    protected Model buildCurrentModel(InterpretationContext interpretationContext, String name, Attributes attributes) {
        System.out.println("building DefineModel");
        DefineModel defineModel = new DefineModel();
        defineModel.setClassName(attributes.getValue(CLASS_ATTRIBUTE));
        defineModel.setName(attributes.getValue(NAME_ATTRIBUTE));
        defineModel.setScopeStr(attributes.getValue(SCOPE_ATTRIBUTE));
        return defineModel;
    }

    @Override
    protected boolean validPreconditions(InterpretationContext ic, String name, Attributes attributes) {
        boolean valid = true;
        String className = attributes.getValue(CLASS_ATTRIBUTE);
        if (OptionHelper.isEmpty(className)) {
            addError("Missing class name for property definer. Near [" + name + "] line " + getLineNumber(ic));
            valid = false;
        }
        
        String propertyName = attributes.getValue(NAME_ATTRIBUTE);
        if (OptionHelper.isEmpty(propertyName)) {
            addError("Missing property name for property definer. Near [" + name + "] line " + getLineNumber(ic));
            valid = false;
        }
        System.out.println("DefinePropertyAction validPreconditions "+ valid);
        return valid;
    }

}
