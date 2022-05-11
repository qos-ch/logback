/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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

import ch.qos.logback.classic.joran.action.ConfigurationAction;
import ch.qos.logback.classic.joran.action.ConsolePluginAction;
import ch.qos.logback.classic.joran.action.ContextNameAction;
import ch.qos.logback.classic.joran.action.InsertFromJNDIAction;
import ch.qos.logback.classic.joran.action.JMXConfiguratorAction;
import ch.qos.logback.classic.joran.action.LevelAction;
import ch.qos.logback.classic.joran.action.LoggerAction;
import ch.qos.logback.classic.joran.action.LoggerContextListenerAction;
import ch.qos.logback.classic.joran.action.ReceiverAction;
import ch.qos.logback.classic.joran.action.RootLoggerAction;
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
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.PlatformInfo;
import ch.qos.logback.classic.util.DefaultNestedComponentRules;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.JoranConfiguratorBase;
import ch.qos.logback.core.joran.action.AppenderRefAction;
import ch.qos.logback.core.joran.action.IncludeAction;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.AppenderRefModel;
import ch.qos.logback.core.model.processor.AppenderModelHandler;
import ch.qos.logback.core.model.processor.AppenderRefDependencyAnalyser;
import ch.qos.logback.core.model.processor.AppenderRefModelHandler;
import ch.qos.logback.core.model.processor.DefaultProcessor;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.model.processor.RefContainerDependencyAnalyser;

/**
 * JoranConfigurator class adds rules specific to logback-classic.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class JoranConfigurator extends JoranConfiguratorBase<ILoggingEvent> {

    @Override
    public void addInstanceRules(RuleStore rs) {
        // add parent rules
        super.addInstanceRules(rs);

        rs.addRule(new ElementSelector("configuration"), () -> new ConfigurationAction());

        rs.addRule(new ElementSelector("configuration/contextName"), () -> new ContextNameAction());
        rs.addRule(new ElementSelector("configuration/contextListener"), () -> new LoggerContextListenerAction());
        rs.addRule(new ElementSelector("configuration/insertFromJNDI"), () -> new InsertFromJNDIAction());

        rs.addRule(new ElementSelector("configuration/logger"), () -> new LoggerAction());
        rs.addRule(new ElementSelector("configuration/logger/level"), () -> new LevelAction());

        rs.addRule(new ElementSelector("configuration/root"), () -> new RootLoggerAction());
        rs.addRule(new ElementSelector("configuration/root/level"), () -> new LevelAction());
        rs.addRule(new ElementSelector("configuration/logger/appender-ref"), () -> new AppenderRefAction());
        rs.addRule(new ElementSelector("configuration/root/appender-ref"), () -> new AppenderRefAction());
        
        if (PlatformInfo.hasJMXObjectName()) {
            rs.addRule(new ElementSelector("configuration/jmxConfigurator"), () -> new JMXConfiguratorAction());
        }
        rs.addRule(new ElementSelector("configuration/include"), () -> new IncludeAction());

        rs.addRule(new ElementSelector("configuration/consolePlugin"), () -> new ConsolePluginAction());

        rs.addRule(new ElementSelector("configuration/receiver"), () -> new ReceiverAction());

    }

    @Override
    protected void addDefaultNestedComponentRegistryRules(DefaultNestedComponentRegistry registry) {
        DefaultNestedComponentRules.addDefaultNestedComponentRegistryRules(registry);
    }

    @Override
    protected DefaultProcessor buildDefaultProcessor(Context context, ModelInterpretationContext mic) {
        DefaultProcessor defaultProcessor = super.buildDefaultProcessor(context, mic);
        defaultProcessor.addHandler(ConfigurationModel.class, ConfigurationModelHandler::makeInstance);
        defaultProcessor.addHandler(ContextNameModel.class, ContextNameModelHandler::makeInstance);
        defaultProcessor.addHandler(LoggerContextListenerModel.class, LoggerContextListenerModelHandler::makeInstance);

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

        defaultProcessor.addAnalyser(AppenderRefModel.class, 
                () -> new AppenderRefDependencyAnalyser(context));

        closeModelFilters(defaultProcessor);

        return defaultProcessor;
    }

    private void closeModelFilters(DefaultProcessor defaultProcessor) {
        defaultProcessor.getPhaseOneFilter().denyAll();
        defaultProcessor.getPhaseTwoFilter().allowAll();
    }
    
//    private void injectModelFilters(DefaultProcessor defaultProcessor) {
//        @SuppressWarnings("unchecked")
//        Class<? extends Model>[] importModelClasses = new Class[] { ImportModel.class };
//
//        @SuppressWarnings("unchecked")
//        Class<? extends Model>[] variableDefinitionModelClasses = new Class[] { ContextNameModel.class,
//                DefineModel.class, PropertyModel.class, TimestampModel.class, ParamModel.class };
//
//        @SuppressWarnings("unchecked")
//        Class<? extends Model>[] implicitModelClasses = new Class[] { ImplicitModel.class };
//
//        @SuppressWarnings("unchecked")
//        Class<? extends Model>[] otherFirstPhaseModelClasses = new Class[] { ConfigurationModel.class,
//                EventEvaluatorModel.class, LoggerContextListenerModel.class, ShutdownHookModel.class,
//                IncludeModel.class, IfModel.class, ThenModel.class, ElseModel.class};
//
//        // MOTE: AppenderModelHandler is delayed to second phase
//
//        ChainedModelFilter fistPhaseDefintionFilter = new ChainedModelFilter();
//        for (Class<? extends Model> modelClass : importModelClasses)
//            fistPhaseDefintionFilter.allow(modelClass);
//        for (Class<? extends Model> modelClass : variableDefinitionModelClasses)
//            fistPhaseDefintionFilter.allow(modelClass);
//        for (Class<? extends Model> modelClass : otherFirstPhaseModelClasses)
//            fistPhaseDefintionFilter.allow(modelClass);
//        for (Class<? extends Model> modelClass : implicitModelClasses)
//            fistPhaseDefintionFilter.allow(modelClass);
//
//        fistPhaseDefintionFilter.denyAll();
//        defaultProcessor.setPhaseOneFilter(fistPhaseDefintionFilter);
//
//        ChainedModelFilter secondPhaseDefinitionFilter = new ChainedModelFilter();
//        secondPhaseDefinitionFilter.allowAll();
//
//        defaultProcessor.setPhaseTwoFilter(secondPhaseDefinitionFilter);
//
//    }

}
