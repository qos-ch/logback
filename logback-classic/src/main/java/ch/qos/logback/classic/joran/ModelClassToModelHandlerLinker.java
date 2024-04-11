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

package ch.qos.logback.classic.joran;

import ch.qos.logback.classic.model.ConfigurationModel;
import ch.qos.logback.classic.model.ContextNameModel;
import ch.qos.logback.classic.model.LevelModel;
import ch.qos.logback.classic.model.LoggerContextListenerModel;
import ch.qos.logback.classic.model.LoggerModel;
import ch.qos.logback.classic.model.RootLoggerModel;
import ch.qos.logback.classic.model.processor.ConfigurationModelHandler;
import ch.qos.logback.classic.model.processor.ContextNameModelHandler;
import ch.qos.logback.classic.model.processor.LevelModelHandler;
import ch.qos.logback.classic.model.processor.LoggerContextListenerModelHandler;
import ch.qos.logback.classic.model.processor.LoggerModelHandler;
import ch.qos.logback.classic.model.processor.RootLoggerModelHandler;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.ModelClassToModelHandlerLinkerBase;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.AppenderRefModel;
import ch.qos.logback.core.model.InsertFromJNDIModel;
import ch.qos.logback.core.model.ModelHandlerFactoryMethod;
import ch.qos.logback.core.model.processor.AppenderModelHandler;
import ch.qos.logback.core.model.processor.AppenderRefDependencyAnalyser;
import ch.qos.logback.core.model.processor.AppenderRefModelHandler;
import ch.qos.logback.core.model.processor.DefaultProcessor;
import ch.qos.logback.core.model.processor.InsertFromJNDIModelHandler;
import ch.qos.logback.core.model.processor.RefContainerDependencyAnalyser;

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

        defaultProcessor.addHandler(InsertFromJNDIModel.class, InsertFromJNDIModelHandler::makeInstance);

        defaultProcessor.addHandler(AppenderModel.class, AppenderModelHandler::makeInstance);
        defaultProcessor.addHandler(AppenderRefModel.class, AppenderRefModelHandler::makeInstance);
        defaultProcessor.addHandler(RootLoggerModel.class, RootLoggerModelHandler::makeInstance);
        defaultProcessor.addHandler(LoggerModel.class, LoggerModelHandler::makeInstance);
        defaultProcessor.addHandler(LevelModel.class, LevelModelHandler::makeInstance);

        defaultProcessor.addAnalyser(LoggerModel.class,
                () -> new RefContainerDependencyAnalyser(context, LoggerModel.class));

        defaultProcessor.addAnalyser(RootLoggerModel.class,
                () -> new RefContainerDependencyAnalyser(context, RootLoggerModel.class));

        defaultProcessor.addAnalyser(AppenderModel.class,
                () -> new RefContainerDependencyAnalyser(context, AppenderModel.class));

        defaultProcessor.addAnalyser(AppenderRefModel.class, () -> new AppenderRefDependencyAnalyser(context));

        sealModelFilters(defaultProcessor);

    }

    public ModelHandlerFactoryMethod getConfigurationModelHandlerFactoryMethod() {
        if(configurationModelHandlerFactoryMethod == null) {
            //System.out.println("returning default ConfigurationModelHandler::makeInstance;");
            return  ConfigurationModelHandler::makeInstance;
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
