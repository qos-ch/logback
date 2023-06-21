/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2023, QOS.ch. All rights reserved.
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

package ch.qos.logback.access.joran;

import ch.qos.logback.access.model.ConfigurationModel;
import ch.qos.logback.access.model.processor.ConfigurationModelHandler;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.ModelClassToModelHandlerLinkerBase;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.AppenderRefModel;
import ch.qos.logback.core.model.processor.AppenderModelHandler;
import ch.qos.logback.core.model.processor.AppenderRefDependencyAnalyser;
import ch.qos.logback.core.model.processor.AppenderRefModelHandler;
import ch.qos.logback.core.model.processor.DefaultProcessor;
import ch.qos.logback.core.model.processor.RefContainerDependencyAnalyser;

/**
 * For a given DefaultProcessor instance link a {@link ch.qos.logback.core.model.Model Model} class to a
 * {@link ch.qos.logback.core.model.processor.ModelHandlerBase ModelHandler} instance
 * for logback-access.
 *
 * <p>Will also use links from super class.</p>
 *
 * @since 1.3.9/1.4.9
 */
public class ModelClassToModelHandlerLinker extends ModelClassToModelHandlerLinkerBase {

    public ModelClassToModelHandlerLinker(Context context) {
        super(context);
    }

    public void link(DefaultProcessor defaultProcessor) {
        super.link(defaultProcessor);
        defaultProcessor.addHandler(ConfigurationModel.class, ConfigurationModelHandler::makeInstance);
        defaultProcessor.addHandler(AppenderModel.class, AppenderModelHandler::makeInstance);
        defaultProcessor.addHandler(AppenderRefModel.class, AppenderRefModelHandler::makeInstance);

        defaultProcessor.addAnalyser(AppenderModel.class,
                () -> new RefContainerDependencyAnalyser(context, AppenderModel.class));
        defaultProcessor.addAnalyser(AppenderRefModel.class, () -> new AppenderRefDependencyAnalyser(context));

        sealModelFilters(defaultProcessor);
    }
}
