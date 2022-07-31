/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.ContextAwareBase;

abstract public class ModelHandlerBase extends ContextAwareBase {

    public ModelHandlerBase(Context context) {
        setContext(context);
    }

    /**
     * Subclasses should return the subclass of Model that they expect to handle.
     * 
     * The default implementation assumes that all Model classes are supported. This
     * a very lax assumption which is usually not true.
     * 
     * @return supported model class
     * @see ModelHandlerBase#isSupportedModelType(Model)
     */
    protected Class<? extends Model> getSupportedModelClass() {
        // Assume lax default where all model objects are supported
        return Model.class;
    }

    protected boolean isSupportedModelType(Model model) {
        Class<? extends Model> modelClass = getSupportedModelClass();
        if (modelClass.isInstance(model)) {
            return true;
        } else {
            addError("This handler can only handle models of type [" + modelClass + "]");
            return false;
        }
    }

    abstract public void handle(ModelInterpretationContext intercon, Model model) throws ModelHandlerException;

    public void postHandle(ModelInterpretationContext intercon, Model model) throws ModelHandlerException {
        // let specialized handlers override
    }

    public String toString() {
        return this.getClass().getName();
    }

}
