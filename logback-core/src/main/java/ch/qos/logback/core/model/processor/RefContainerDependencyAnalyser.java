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

@PhaseIndicator(phase = ProcessingPhase.DEPENDENCY_ANALYSIS)
public class RefContainerDependencyAnalyser extends ModelHandlerBase {

    final Class<?> modelClass;

    public RefContainerDependencyAnalyser(Context context, Class<?> modelClass) {
        super(context);
        this.modelClass = modelClass;
    }

    @Override
    protected boolean isSupportedModelType(Model model) {

        if (modelClass.isInstance(model)) {
            return true;
        }

        StringBuilder buf = new StringBuilder("This handler can only handle models of type ");
        buf.append(modelClass.getName());
        addError(buf.toString());
        return false;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        mic.pushModel(model);
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        Model poppedModel = mic.popModel();
        if (model != poppedModel) {
            addError("Popped model [" + poppedModel + "] different than expected [" + model + "]");
        }
    }
}
