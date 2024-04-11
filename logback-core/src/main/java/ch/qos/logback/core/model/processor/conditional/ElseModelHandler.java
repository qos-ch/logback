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
import ch.qos.logback.core.model.conditional.ElseModel;
import ch.qos.logback.core.model.conditional.IfModel;
import ch.qos.logback.core.model.conditional.IfModel.BranchState;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class ElseModelHandler extends ModelHandlerBase {

    public ElseModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new ElseModelHandler(context);
    }

    @Override
    protected Class<ElseModel> getSupportedModelClass() {
        return ElseModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {

        ElseModel elseModel = (ElseModel) model;

        Model parent = mic.peekModel();

        if (!(parent instanceof IfModel)) {
            addError("Unexpected type for parent model [" + parent + "]");
            elseModel.markAsSkipped();
            return;
        }
              
        IfModel ifModel = (IfModel) parent;
        if(ifModel.getBranchState() != BranchState.ELSE_BRANCH) {
            elseModel.deepMarkAsSkipped();
        }
    }

}
