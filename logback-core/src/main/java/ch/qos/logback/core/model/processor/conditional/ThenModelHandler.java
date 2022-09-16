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
package ch.qos.logback.core.model.processor.conditional;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.conditional.IfModel;
import ch.qos.logback.core.model.conditional.IfModel.BranchState;
import ch.qos.logback.core.model.conditional.ThenModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

import static ch.qos.logback.core.spi.ErrorCodes.MISSING_IF_EMPTY_MODEL_STACK;

public class ThenModelHandler extends ModelHandlerBase {

    public ThenModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new ThenModelHandler(context);
    }

    @Override
    protected Class<ThenModel> getSupportedModelClass() {
        return ThenModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {

        ThenModel thenModel = (ThenModel) model;

        if(mic.isModelStackEmpty()) {
            addError(MISSING_IF_EMPTY_MODEL_STACK);
            thenModel.markAsSkipped();
            return;
        }
        Model parent = mic.peekModel();

        if (!(parent instanceof IfModel)) {
            addError("Unexpected type for parent model [" + parent + "]");
            thenModel.markAsSkipped();
            return;
        }
              
        IfModel ifModel = (IfModel) parent;
        if(ifModel.getBranchState() != BranchState.IF_BRANCH) {
            thenModel.deepMarkAsSkipped();
        }
    }

}
