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

package ch.qos.logback.core.joran.implicitAction;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class FruitContextModelHandler extends ModelHandlerBase {

    public FruitContextModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new FruitContextModelHandler(context);
    }

    @Override
    public void handle(ModelInterpretationContext interpretationContext, Model model) throws ModelHandlerException {
        interpretationContext.pushObject(context);
    }

    @Override
    public void postHandle(ModelInterpretationContext ec, Model model) throws ModelHandlerException {

        Object o = ec.peekObject();

        if (o != context) {
            addWarn("The object [" + o + "] at top of the stack is not the context named [" + context.getName()
                    + "] pushed earlier.");
        } else {
            addInfo("Popping context named [" + context.getName() + "] from the object stack");
            ec.popObject();
        }
    }

}
