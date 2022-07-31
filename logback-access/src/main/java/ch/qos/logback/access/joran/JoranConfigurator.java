/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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

import ch.qos.logback.access.joran.action.ConfigurationAction;
import ch.qos.logback.access.model.ConfigurationModel;
import ch.qos.logback.access.model.processor.ConfigurationModelHandler;
import ch.qos.logback.access.model.processor.LogbackAccessDefaultNestedComponentRegistryRules;
import ch.qos.logback.access.spi.IAccessEvent;
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
import ch.qos.logback.core.model.processor.RefContainerDependencyAnalyser;

/**
 * This JoranConfiguratorclass adds rules specific to logback-access.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class JoranConfigurator extends JoranConfiguratorBase<IAccessEvent> {

    @Override
    public void addElementSelectorAndActionAssociations(RuleStore rs) {
        super.addElementSelectorAndActionAssociations(rs);

        rs.addRule(new ElementSelector("configuration"), () -> new ConfigurationAction());
        rs.addRule(new ElementSelector("configuration/appender-ref"), () -> new AppenderRefAction());
        rs.addRule(new ElementSelector("configuration/include"), () -> new IncludeAction());
    }

    @Override
    protected void addModelHandlerAssociations(DefaultProcessor defaultProcessor) {
        super.addModelHandlerAssociations(defaultProcessor);
        defaultProcessor.addHandler(ConfigurationModel.class, ConfigurationModelHandler::makeInstance);
        defaultProcessor.addHandler(AppenderModel.class, AppenderModelHandler::makeInstance); 
        defaultProcessor.addHandler(AppenderRefModel.class, AppenderRefModelHandler::makeInstance);

        defaultProcessor.addAnalyser(AppenderModel.class, () -> 
                new RefContainerDependencyAnalyser(context, AppenderModel.class));
        defaultProcessor.addAnalyser(AppenderRefModel.class, () -> new AppenderRefDependencyAnalyser(context));

        sealModelFilters(defaultProcessor);
    }

    // The final filters in the two filter chain are rather crucial.
    // They ensure that only Models attached to the firstPhaseFilter will
    // be handled in the first phase and all models not previously handled
    // in the second phase will be handled in a catch-all fallback case.
    private void sealModelFilters(DefaultProcessor defaultProcessor) {
        defaultProcessor.getPhaseOneFilter().denyAll();
        defaultProcessor.getPhaseTwoFilter().allowAll();
    }

    @Override
    protected void addDefaultNestedComponentRegistryRules(DefaultNestedComponentRegistry registry) {
        LogbackAccessDefaultNestedComponentRegistryRules.addDefaultNestedComponentRegistryRules(registry);    
    }

}
