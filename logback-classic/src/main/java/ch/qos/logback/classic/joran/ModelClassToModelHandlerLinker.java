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

package ch.qos.logback.classic.joran;

import ch.qos.logback.classic.model.*;
import ch.qos.logback.classic.model.processor.*;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.ModelClassToModelHandlerLinkerBase;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.AppenderRefModel;
import ch.qos.logback.core.model.InsertFromJNDIModel;
import ch.qos.logback.core.model.ModelHandlerFactoryMethod;
import ch.qos.logback.core.model.processor.*;

/**
 * For a given DefaultProcessor instance link a {@link ch.qos.logback.core.model.Model Model} class to a
 * {@link ch.qos.logback.core.model.processor.ModelHandlerBase ModelHandler} instance for
 * logback-classic.
 *
 * <p>Will also use links from super class.</p>
 *
 * @since 1.3.9/1.4.9
 */
public class ModelClassToModelHandlerLinker extends ModelClassToModelHandlerLinkerBase {

    public ModelClassToModelHandlerLinker(Context context) {
        super(context);
    }

    ModelHandlerFactoryMethod configurationModelHandlerFactoryMethod;

    @Override
    public void link(DefaultProcessor defaultProcessor) {
        super.link(defaultProcessor);
        defaultProcessor.addHandler(ConfigurationModel.class, getConfigurationModelHandlerFactoryMethod());
        defaultProcessor.addHandler(ContextNameModel.class, ContextNameModelHandler::makeInstance);
        defaultProcessor.addHandler(LoggerContextListenerModel.class, LoggerContextListenerModelHandler::makeInstance);

        defaultProcessor.addHandler(PropertiesConfiguratorModel.class, PropertiesConfiguratorModelHandler::makeInstance);
        defaultProcessor.addHandler(InsertFromJNDIModel.class, InsertFromJNDIModelHandler::makeInstance);

        defaultProcessor.addHandler(AppenderModel.class, AppenderModelHandler::makeInstance);
        defaultProcessor.addHandler(AppenderRefModel.class, AppenderRefModelHandler::makeInstance);
        defaultProcessor.addHandler(RootLoggerModel.class, RootLoggerModelHandler::makeInstance);
        defaultProcessor.addHandler(LoggerModel.class, LoggerModelHandler::makeInstance);
        defaultProcessor.addHandler(LevelModel.class, LevelModelHandler::makeInstance);
        defaultProcessor.addHandler(ReceiverModel.class, ReceiverModelHandler::makeInstance);

        defaultProcessor.addAnalyser(RootLoggerModel.class,
                () -> new AppenderRefDependencyAnalyser(context));

        defaultProcessor.addAnalyser(LoggerModel.class,
                () -> new AppenderRefDependencyAnalyser(context));

        // an appender may contain appender refs, e.g. AsyncAppender
        defaultProcessor.addAnalyser(AppenderModel.class,
                () -> new AppenderRefDependencyAnalyser(context));

        defaultProcessor.addAnalyser(AppenderModel.class, () -> new FileCollisionAnalyser(context));


        defaultProcessor.addAnalyser(AppenderModel.class, () -> new AppenderDeclarationAnalyser(context));

        sealModelFilters(defaultProcessor);

    }

    public ModelHandlerFactoryMethod getConfigurationModelHandlerFactoryMethod() {
        if (configurationModelHandlerFactoryMethod == null) {
            //System.out.println("returning default ConfigurationModelHandler::makeInstance;");
            return ConfigurationModelHandler::makeInstance;
        } else {
            //System.out.println("returning set "+configurationModelHandlerFactoryMethod);
            return configurationModelHandlerFactoryMethod;
        }
    }


    /**
     * Allow configurators to override the factory method for ConfigurationModelHandler
     *
     */
    public void setConfigurationModelHandlerFactoryMethod(ModelHandlerFactoryMethod cmhfm) {
        //System.out.println("setConfigurationModelHandlerFactoryMethod called with "+cmhfm);
        this.configurationModelHandlerFactoryMethod = cmhfm;
    }

}
