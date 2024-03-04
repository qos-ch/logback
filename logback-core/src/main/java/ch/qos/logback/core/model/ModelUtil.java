/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.model;

import ch.qos.logback.core.joran.action.ActionUtil.Scope;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.model.util.PropertyModelHandlerHelper;

import java.util.Properties;

public class ModelUtil {

    
    static public void resetForReuse(Model model) {
        if(model == null)
           return;
        model.resetForReuse();
    }



    /**
     * Add all the properties found in the argument named 'props' to an
     * ModelInterpretationContext.
     *
     * @deprecated moved to {@link PropertyModelHandlerHelper#setProperty}
     */
    @Deprecated
    static public void setProperty(ModelInterpretationContext mic, String key, String value, Scope scope) {
        PropertyModelHandlerHelper.setProperty(mic, key, value, scope);
    }

    /**
     * Add all the properties found in the argument named 'props' to an
     * ModelInterpretationContext.
     *
     * @deprecated   moved to {@link PropertyModelHandlerHelper#setProperties}
     */
    @Deprecated
    static public void setProperties(ModelInterpretationContext mic, Properties props, Scope scope) {
        PropertyModelHandlerHelper.setProperties(mic, props, scope);
    }

}
