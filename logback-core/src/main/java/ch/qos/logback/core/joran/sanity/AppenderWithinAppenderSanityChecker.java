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
import java.util.stream.Collectors;

public class AppenderWithinAppenderSanityChecker extends ContextAwareBase implements SanityChecker  {

    static public String NESTED_APPENDERS_WARNING = "As of logback version 1.3, nested appenders are not allowed.";

    @Override
    public void check(Model model) {
        if (model == null)
            return;

        List<Model> appenderModels = new ArrayList<>();
        deepFindAllModelsOfType(AppenderModel.class, appenderModels, model);

        List<Pair<Model, Model>> nestedPairs = deepFindNestedSubModelsOfType(AppenderModel.class, appenderModels);

        List<Pair<Model, Model>> filteredNestedPairs = nestedPairs.stream().filter(pair -> !isSiftingAppender(pair.first)).collect(Collectors.toList());

        if(filteredNestedPairs.isEmpty()) {
            return;
        }
        addWarn(NESTED_APPENDERS_WARNING);
        for(Pair<Model, Model> pair: filteredNestedPairs) {
            addWarn("Appender at line "+pair.first.getLineNumber() + " contains a nested appender at line "+pair.second.getLineNumber());
        }
    }

    private boolean isSiftingAppender(Model first) {
        if(first instanceof  AppenderModel) {
            AppenderModel appenderModel = (AppenderModel) first;
            String classname = appenderModel.getClassName();
            if(classname == null)
                return false;
            return appenderModel.getClassName().contains("SiftingAppender");
        }
        return false;
    }

}
