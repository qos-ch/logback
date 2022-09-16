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
package ch.qos.logback.core.joran.sanity;

import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.ContextAwareBase;

import java.util.ArrayList;
import java.util.List;

public class AppenderWithinAppenderSanityChecker extends ContextAwareBase implements SanityChecker  {

    static public String NESTED_APPENDERS_WARNING = "As of logback version 1.3, nested appenders are not allowed.";

    @Override
    public void check(Model model) {
        if (model == null)
            return;

        List<AppenderModel> appenderModels = new ArrayList<>();
        deepFindAllModelsOfType(AppenderModel.class, appenderModels, model);

        List<String> warnings = new ArrayList<>();


        for(AppenderModel appenderModel: appenderModels) {
            List<AppenderModel> nestedAppenders = new ArrayList<>();

            appenderModel.getSubModels().stream().forEach( m -> deepFindAllModelsOfType(AppenderModel.class, nestedAppenders, m));

            if(!nestedAppenders.isEmpty()) {
                AppenderModel inner = nestedAppenders.get(0);
                warnings.add("Appender at line "+appenderModel.getLineNumber() + " contains nested appenders.");
                warnings.add("First nested appender occurrence at line "+inner.getLineNumber());
            }
        }

        if(warnings.isEmpty())
            return;

        addWarn(NESTED_APPENDERS_WARNING);
        warnings.forEach( w -> addWarn(w));
    }

    private <T extends Model> void deepFindAllModelsOfType(Class<T> modelClass, List<T> modelList, Model model) {
        if(modelClass.isInstance(model)) {
            modelList.add((T) model);
        }

        for(Model m: model.getSubModels()) {
            deepFindAllModelsOfType(modelClass, modelList, m);
        }
    }
}
