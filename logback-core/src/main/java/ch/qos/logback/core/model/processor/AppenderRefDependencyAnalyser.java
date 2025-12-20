/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
 * <p>
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 * <p>
 * or (per the licensee's choosing)
 * <p>
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.AppenderRefModel;
import ch.qos.logback.core.model.Model;

import java.util.List;
import java.util.stream.Collectors;

@PhaseIndicator(phase = ProcessingPhase.DEPENDENCY_ANALYSIS)
public class AppenderRefDependencyAnalyser extends ModelHandlerBase {

    public AppenderRefDependencyAnalyser(Context context) {
        super(context);
    }

    @Override
    protected Class<Model> getSupportedModelClass() {
        return Model.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model parentModel) throws ModelHandlerException {

        List<AppenderRefModel> appenderRefModels =
                parentModel.getSubModels().stream().filter(m -> m instanceof AppenderRefModel).map(m -> (AppenderRefModel) m).collect(Collectors.toList());


        for (AppenderRefModel appenderRefModel : appenderRefModels) {
            String ref = mic.subst(appenderRefModel.getRef());
            DependencyDefinition dd = new DependencyDefinition(parentModel, ref);
            mic.addDependencyDefinition(dd);
        }

    }

}
