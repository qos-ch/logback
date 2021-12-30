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
package ch.qos.logback.access.joran;

import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.access.PatternLayoutEncoder;
import ch.qos.logback.access.boolex.JaninoEventEvaluator;
import ch.qos.logback.access.joran.action.ConfigurationAction;
import ch.qos.logback.access.model.ConfigurationModel;
import ch.qos.logback.access.model.processor.ConfigurationModelHandler;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.joran.JoranConfiguratorBase;
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
import ch.qos.logback.core.model.ImportModel;
import ch.qos.logback.core.model.IncludeModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ParamModel;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.model.ShutdownHookModel;
import ch.qos.logback.core.model.TimestampModel;
import ch.qos.logback.core.model.processor.AllowAllModelFilter;
import ch.qos.logback.core.model.processor.AppenderModelHandler;
import ch.qos.logback.core.model.processor.AppenderRefDependencyAnalyser;
import ch.qos.logback.core.model.processor.AppenderRefModelHandler;
import ch.qos.logback.core.model.processor.ChainedModelFilter;
import ch.qos.logback.core.model.processor.DefaultProcessor;
import ch.qos.logback.core.model.processor.RefContainerDependencyAnalyser;
import ch.qos.logback.core.net.ssl.SSLNestedComponentRegistryRules;

/**
 * This JoranConfiguratorclass adds rules specific to logback-access.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class JoranConfigurator extends JoranConfiguratorBase<IAccessEvent> {

    @Override
    public void addInstanceRules(RuleStore rs) {
        super.addInstanceRules(rs);

        rs.addRule(new ElementSelector("configuration"), new ConfigurationAction());
        rs.addRule(new ElementSelector("configuration/appender-ref"), new AppenderRefAction());
        rs.addRule(new ElementSelector("configuration/include"), new IncludeModelAction());
    }

    @Override
    protected DefaultProcessor buildDefaultProcessor(Context context, InterpretationContext interpretationContext) {
    	DefaultProcessor defaultProcessor = super.buildDefaultProcessor(context, interpretationContext);
        defaultProcessor.addHandler(ConfigurationModel.class, ConfigurationModelHandler::makeInstance);
        defaultProcessor.addHandler(AppenderModel.class, AppenderModelHandler::makeInstance);
        defaultProcessor.addHandler(AppenderRefModel.class, AppenderRefModelHandler::makeInstance);
        
        
        defaultProcessor.addAnalyser(AppenderModel.class, new RefContainerDependencyAnalyser(context, AppenderModel.class));
        defaultProcessor.addAnalyser(AppenderRefModel.class, new AppenderRefDependencyAnalyser(context));
 
        injectModelFilters(defaultProcessor);
        
    	return defaultProcessor;
    	
    }

	private void injectModelFilters(DefaultProcessor defaultProcessor) {
		@SuppressWarnings("unchecked")
		Class<? extends Model>[] importModelClasses = new Class[] { ImportModel.class };
		
		
		@SuppressWarnings("unchecked")
		Class<? extends Model>[] variableDefinitionModelClasses = new Class[] { 
				DefineModel.class, 
				PropertyModel.class, 
				TimestampModel.class, 
				ParamModel.class};

		@SuppressWarnings("unchecked")
		Class<? extends Model>[] implicitModelClasses = new Class[] { 
				ImplicitModel.class, 
				ParamModel.class};

		@SuppressWarnings("unchecked")
		Class<? extends Model>[] otherFirstPhaseModelClasses = new Class[] { 
				ConfigurationModel.class, 
				EventEvaluatorModel.class,
				ShutdownHookModel.class, 
				EventEvaluatorModel.class, 
				IncludeModel.class,
				};



		ChainedModelFilter fistPhaseDefintionFilter = new ChainedModelFilter();
		for (Class<? extends Model> modelClass : importModelClasses)
			fistPhaseDefintionFilter.allow(modelClass);
		for (Class<? extends Model> modelClass : variableDefinitionModelClasses)
			fistPhaseDefintionFilter.allow(modelClass);
		for (Class<? extends Model> modelClass : otherFirstPhaseModelClasses)
			fistPhaseDefintionFilter.allow(modelClass);
		for (Class<? extends Model> modelClass : implicitModelClasses)
			fistPhaseDefintionFilter.allow(modelClass);
		
		fistPhaseDefintionFilter.denyAll();
		defaultProcessor.setPhaseOneFilter(fistPhaseDefintionFilter);

		// Note: AppenderModel is in the second phase

		defaultProcessor.setPhaseTwoFilter(new AllowAllModelFilter());

	}
	
	
    @Override
    protected void addDefaultNestedComponentRegistryRules(DefaultNestedComponentRegistry registry) {
        registry.add(AppenderBase.class, "layout", PatternLayout.class);
        registry.add(EvaluatorFilter.class, "evaluator", JaninoEventEvaluator.class);

        registry.add(AppenderBase.class, "encoder", PatternLayoutEncoder.class);
        registry.add(UnsynchronizedAppenderBase.class, "encoder", PatternLayoutEncoder.class);
        SSLNestedComponentRegistryRules.addDefaultNestedComponentRegistryRules(registry);
    }

}
