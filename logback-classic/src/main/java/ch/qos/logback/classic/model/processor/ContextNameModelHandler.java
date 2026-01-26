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

package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.model.ContextNameModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class ContextNameModelHandler extends ModelHandlerBase {

    public ContextNameModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new ContextNameModelHandler(context);
    }

    @Override
    protected Class<ContextNameModel> getSupportedModelClass() {
        return ContextNameModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        ContextNameModel contextNameModel = (ContextNameModel) model;

        String finalBody = mic.subst(contextNameModel.getBodyText());
        addInfo("Setting logger context name as [" + finalBody + "]");
        try {
            context.setName(finalBody);
        } catch (IllegalStateException e) {
            addError("Failed to rename context [" + context.getName() + "] as [" + finalBody + "]", e);
        }

    }

}
