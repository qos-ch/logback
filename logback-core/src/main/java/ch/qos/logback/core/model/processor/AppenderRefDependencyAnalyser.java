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
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.AppenderRefModel;
import ch.qos.logback.core.model.Model;

import java.util.List;

/**
 * The AppenderRefDependencyAnalyser class is responsible for analyzing dependencies
 * related to appender references within a logging model. This class extends
 * ModelHandlerBase and operates during the dependency analysis phase of processing.
 *
 * The primary responsibilities of this class include:
 * - Identifying instances of {@link AppenderRefModel} within a model hierarchy.
 * - Substituting references to appender models using the context's interpretation logic.
 * - Adding dependency definitions for the identified appender references to the interpretation context.
 */
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
        List<AppenderRefModel> appenderRefModelList = new java.util.ArrayList<>();
        collectAllAppenderRefModels(appenderRefModelList, parentModel);

        for (AppenderRefModel appenderRefModel : appenderRefModelList) {
            // TODO: prevent substitution of references
            String ref = appenderRefModel.getRef();
            DependencyDefinition dd = new DependencyDefinition(parentModel, ref);
            mic.addDependencyDefinition(dd);
        }

    }

    /**
     * Recursively processes the given Model object and its submodels, extracting instances
     * of AppenderRefModel and adding them to the provided list.
     *
     * @param list the list to which AppenderRefModel instances are added
     * @param model the root Model object from which to start the extraction
     */
    public void collectAllAppenderRefModels(List<AppenderRefModel> list, Model model) {
        if(model == null)
            return;
        if(model instanceof AppenderRefModel) {
            list.add((AppenderRefModel) model);
        }
        model.getSubModels().forEach(subModel -> collectAllAppenderRefModels(list, subModel));
    }

}
