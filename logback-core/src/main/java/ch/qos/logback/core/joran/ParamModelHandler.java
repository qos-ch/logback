/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.core.joran;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.util.PropertySetter;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ParamModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class ParamModelHandler extends ModelHandlerBase {

    private final BeanDescriptionCache beanDescriptionCache;

    public ParamModelHandler(Context context, BeanDescriptionCache beanDescriptionCache) {
        super(context);
        this.beanDescriptionCache = beanDescriptionCache;
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new ParamModelHandler(context, ic.getBeanDescriptionCache());
    }

    @Override
    protected Class<ParamModel> getSupportedModelClass() {
        return ParamModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {

        ParamModel paramModel = (ParamModel) model;

        String valueStr = mic.subst(paramModel.getValue());

        Object o = mic.peekObject();

        PropertySetter propSetter = new PropertySetter(beanDescriptionCache, o);
        propSetter.setContext(context);

        // allow for variable substitution for name as well
        String finalName = mic.subst(paramModel.getName());
        propSetter.setProperty(finalName, valueStr);
    }

}
