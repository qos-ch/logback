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
import ch.qos.logback.core.joran.ParamModelHandler;
import ch.qos.logback.core.joran.action.AppenderRefAction;
import ch.qos.logback.core.joran.action.IncludeModelAction;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.AppenderRefModel;
import ch.qos.logback.core.model.DefineModel;
import ch.qos.logback.core.model.EventEvaluatorModel;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.IncludeModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ParamModel;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.model.ShutdownHookModel;
import ch.qos.logback.core.model.StatusListenerModel;
import ch.qos.logback.core.model.TimestampModel;
import ch.qos.logback.core.model.processor.AllowAllModelFilter;
import ch.qos.logback.core.model.processor.AppenderModelHandler;
import ch.qos.logback.core.model.processor.AppenderRefModelHandler;
import ch.qos.logback.core.model.processor.ChainedModelFilter;
import ch.qos.logback.core.model.processor.DefaultProcessor;
import ch.qos.logback.core.model.processor.DefineModelHandler;
import ch.qos.logback.core.model.processor.EventEvaluatorModelHandler;
import ch.qos.logback.core.model.processor.ImplicitModelHandler;
import ch.qos.logback.core.model.processor.IncludeModelHandler;
import ch.qos.logback.core.model.processor.PropertyModelHandler;
import ch.qos.logback.core.model.processor.ShutdownHookModelHandler;
import ch.qos.logback.core.model.processor.StatusListenerModelHandler;
import ch.qos.logback.core.model.processor.TimestampModelHandler;

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

		rs.addRule(new ElementSelector("configuration"), new ConfigurationAction());

		rs.addRule(new ElementSelector("configuration/contextName"), new ContextNameAction());
		rs.addRule(new ElementSelector("configuration/contextListener"), new LoggerContextListenerAction());
		rs.addRule(new ElementSelector("configuration/insertFromJNDI"), new InsertFromJNDIAction());

		rs.addRule(new ElementSelector("configuration/logger"), new LoggerAction());
		rs.addRule(new ElementSelector("configuration/logger/level"), new LevelAction());

		rs.addRule(new ElementSelector("configuration/root"), new RootLoggerAction());
		rs.addRule(new ElementSelector("configuration/root/level"), new LevelAction());
		rs.addRule(new ElementSelector("configuration/logger/appender-ref"), new AppenderRefAction());
		rs.addRule(new ElementSelector("configuration/root/appender-ref"), new AppenderRefAction());

		// add if-then-else support
		// rs.addRule(new ElementSelector("*/if"), new IfAction());
		// rs.addRule(new ElementSelector("*/if/then"), new ThenAction());
		// rs.addRule(new ElementSelector("*/if/then/*"), new NOPAction());
		// rs.addRule(new ElementSelector("*/if/else"), new ElseAction());
		// rs.addRule(new ElementSelector("*/if/else/*"), new NOPAction());

		// add jmxConfigurator only if we have JMX available.
		// If running under JDK 1.4 (retrotranslateed logback) then we
		// might not have JMX.
		if (PlatformInfo.hasJMXObjectName()) {
			rs.addRule(new ElementSelector("configuration/jmxConfigurator"), new JMXConfiguratorAction());
		}
		rs.addRule(new ElementSelector("configuration/include"), new IncludeModelAction());

		rs.addRule(new ElementSelector("configuration/consolePlugin"), new ConsolePluginAction());

		rs.addRule(new ElementSelector("configuration/receiver"), new ReceiverAction());

	}

	@Override
	protected void addDefaultNestedComponentRegistryRules(DefaultNestedComponentRegistry registry) {
		DefaultNestedComponentRules.addDefaultNestedComponentRegistryRules(registry);
	}

	@Override
	protected DefaultProcessor buildDefaultProcessor(Context context, InterpretationContext interpretationContext) {
		DefaultProcessor defaultProcessor = super.buildDefaultProcessor(context, interpretationContext);
		defaultProcessor.addHandler(ConfigurationModel.class, ConfigurationModelHandler.class);
		defaultProcessor.addHandler(ContextNameModel.class, ContextNameModelHandler.class);
		defaultProcessor.addHandler(LoggerContextListenerModel.class, LoggerContextListenerModelHandler.class);

		defaultProcessor.addHandler(IncludeModel.class, IncludeModelHandler.class);

		defaultProcessor.addHandler(AppenderModel.class, AppenderModelHandler.class);
		defaultProcessor.addHandler(AppenderRefModel.class, AppenderRefModelHandler.class);
		defaultProcessor.addHandler(RootLoggerModel.class, RootLoggerModelHandler.class);
		defaultProcessor.addHandler(LoggerModel.class, LoggerModelHandler.class);
		defaultProcessor.addHandler(LevelModel.class, LevelModelHandler.class);

		injectModelFilters(defaultProcessor);

		return defaultProcessor;
	}

	private void injectModelFilters(DefaultProcessor defaultProcessor) {
	
        defaultProcessor.addHandler(ShutdownHookModel.class, ShutdownHookModelHandler.class);
        defaultProcessor.addHandler(EventEvaluatorModel.class, EventEvaluatorModelHandler.class);
        defaultProcessor.addHandler(DefineModel.class, DefineModelHandler.class);
        
        defaultProcessor.addHandler(ParamModel.class, ParamModelHandler.class);
        defaultProcessor.addHandler(PropertyModel.class, PropertyModelHandler.class);
        defaultProcessor.addHandler(TimestampModel.class, TimestampModelHandler.class);
        defaultProcessor.addHandler(StatusListenerModel.class, StatusListenerModelHandler.class);
        defaultProcessor.addHandler(ImplicitModel.class, ImplicitModelHandler.class);
        

		@SuppressWarnings("unchecked")
		Class<? extends Model>[] variableDefinitionModelClasses = new Class[] { 
				ContextNameModel.class,
				DefineModel.class, 
				PropertyModel.class, 
				TimestampModel.class, 
				ParamModel.class};

		@SuppressWarnings("unchecked")
		Class<? extends Model>[] implicitModelClasses = new Class[] { 
				ImplicitModel.class, 
				ParamModel.class};

		@SuppressWarnings("unchecked")
		Class<? extends Model>[] loggerModelClasses = new Class[] { 
				LoggerModel.class, 
				RootLoggerModel.class,
				AppenderRefModel.class };

		@SuppressWarnings("unchecked")
		Class<? extends Model>[] otherFirstPhaseModelClasses = new Class[] { 
				ConfigurationModel.class, 
				EventEvaluatorModel.class,
				LoggerContextListenerModel.class,
				ShutdownHookModel.class, 
				EventEvaluatorModel.class, 
				IncludeModel.class,
				};



		ChainedModelFilter fistPhaseDefintionFilter = new ChainedModelFilter();

		for (Class<? extends Model> modelClass : variableDefinitionModelClasses)
			fistPhaseDefintionFilter.allow(modelClass);
		for (Class<? extends Model> modelClass : otherFirstPhaseModelClasses)
			fistPhaseDefintionFilter.allow(modelClass);
		for (Class<? extends Model> modelClass : implicitModelClasses)
			fistPhaseDefintionFilter.allow(modelClass);
		for (Class<? extends Model> modelClass : loggerModelClasses)
			fistPhaseDefintionFilter.allow(modelClass);
		
		fistPhaseDefintionFilter.denyAll();
		defaultProcessor.setPhaseOneFilter(fistPhaseDefintionFilter);


		defaultProcessor.setPhaseTwoFilter(new AllowAllModelFilter());
		
	}

}
