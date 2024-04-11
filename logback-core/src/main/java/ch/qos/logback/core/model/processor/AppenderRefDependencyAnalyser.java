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
import ch.qos.logback.core.model.AppenderRefModel;
import ch.qos.logback.core.model.Model;

@PhaseIndicator(phase = ProcessingPhase.DEPENDENCY_ANALYSIS)
public class AppenderRefDependencyAnalyser extends ModelHandlerBase {

    public AppenderRefDependencyAnalyser(Context context) {
        super(context);
    }

    @Override
    protected Class<AppenderRefModel> getSupportedModelClass() {
        return AppenderRefModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {

        AppenderRefModel appenderRefModel = (AppenderRefModel) model;

        String ref = mic.subst(appenderRefModel.getRef());

        Model depender;
        if (mic.isModelStackEmpty()) {
            // appenderRefModel maybe the dependent model. This is the case in logback-access
            depender = appenderRefModel;
        } else {
            Model parentModel = mic.peekModel();
            depender = parentModel;
        }

        DependencyDefinition dd = new DependencyDefinition(depender, ref);
        mic.addDependencyDefinition(dd);
        
    }

}
