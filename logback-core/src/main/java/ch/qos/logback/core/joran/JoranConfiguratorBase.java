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
package ch.qos.logback.core.joran;

import ch.qos.logback.core.joran.action.AppenderAction;
import ch.qos.logback.core.joran.action.AppenderRefAction;
import ch.qos.logback.core.joran.action.ContextPropertyAction;
import ch.qos.logback.core.joran.action.ConversionRuleAction;
import ch.qos.logback.core.joran.action.DefinePropertyAction;
import ch.qos.logback.core.joran.action.EventEvaluatorAction;
import ch.qos.logback.core.joran.action.ImplicitModelAction;
import ch.qos.logback.core.joran.action.ImportAction;
import ch.qos.logback.core.joran.action.NewRuleAction;
import ch.qos.logback.core.joran.action.ParamAction;
import ch.qos.logback.core.joran.action.PropertyAction;
import ch.qos.logback.core.joran.action.ShutdownHookAction;
import ch.qos.logback.core.joran.action.SiftAction;
import ch.qos.logback.core.joran.action.StatusListenerAction;
import ch.qos.logback.core.joran.action.TimestampAction;
import ch.qos.logback.core.joran.conditional.ElseAction;
import ch.qos.logback.core.joran.conditional.IfAction;
import ch.qos.logback.core.joran.conditional.ThenAction;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;
import ch.qos.logback.core.model.DefineModel;
import ch.qos.logback.core.model.EventEvaluatorModel;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.ImportModel;
import ch.qos.logback.core.model.IncludeModel;
import ch.qos.logback.core.model.ParamModel;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.model.ShutdownHookModel;
import ch.qos.logback.core.model.SiftModel;
import ch.qos.logback.core.model.StatusListenerModel;
import ch.qos.logback.core.model.TimestampModel;
import ch.qos.logback.core.model.conditional.ElseModel;
import ch.qos.logback.core.model.conditional.IfModel;
import ch.qos.logback.core.model.conditional.ThenModel;
import ch.qos.logback.core.model.processor.DefaultProcessor;
import ch.qos.logback.core.model.processor.DefineModelHandler;
import ch.qos.logback.core.model.processor.EventEvaluatorModelHandler;
import ch.qos.logback.core.model.processor.ImplicitModelHandler;
import ch.qos.logback.core.model.processor.ImportModelHandler;
import ch.qos.logback.core.model.processor.NOPModelHandler;
import ch.qos.logback.core.model.processor.PropertyModelHandler;
import ch.qos.logback.core.model.processor.ShutdownHookModelHandler;
import ch.qos.logback.core.model.processor.StatusListenerModelHandler;
import ch.qos.logback.core.model.processor.TimestampModelHandler;
import ch.qos.logback.core.model.processor.conditional.ElseModelHandler;
import ch.qos.logback.core.model.processor.conditional.IfModelHandler;
import ch.qos.logback.core.model.processor.conditional.ThenModelHandler;
import ch.qos.logback.core.sift.SiftModelHandler;

// Based on 310985 revision 310985 as attested by http://tinyurl.com/8njps
// see also http://tinyurl.com/c2rp5

/**
 * A JoranConfiguratorBase lays most of the groundwork for concrete
 * configurators derived from it. Concrete configurators only need to implement
 * the {@link #addElementSelectorAndActionAssociations} method.
 * <p>
 * A JoranConfiguratorBase instance should not be used more than once to
 * configure a Context.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
abstract public class JoranConfiguratorBase<E> extends GenericXMLConfigurator {

    @Override
    protected void addElementSelectorAndActionAssociations(RuleStore rs) {

        // is "*/variable" referenced in the docs?
        rs.addRule(new ElementSelector("*/variable"), PropertyAction::new);
        rs.addRule(new ElementSelector("*/property"),  PropertyAction::new);
        // substitutionProperty is deprecated
        rs.addRule(new ElementSelector("*/substitutionProperty"),  PropertyAction::new);

        rs.addRule(new ElementSelector("configuration/import"), ImportAction::new);
        

        rs.addRule(new ElementSelector("configuration/timestamp"),  TimestampAction::new);
        rs.addRule(new ElementSelector("configuration/shutdownHook"),  ShutdownHookAction::new);
        rs.addRule(new ElementSelector("configuration/define"),  DefinePropertyAction::new);
        rs.addRule(new ElementSelector("configuration/evaluator"),  EventEvaluatorAction::new);

        // the contextProperty pattern is deprecated. It is undocumented
        // and will be dropped in future versions of logback
        rs.addRule(new ElementSelector("configuration/contextProperty"),  ContextPropertyAction::new);

        rs.addRule(new ElementSelector("configuration/conversionRule"),  ConversionRuleAction::new);

        rs.addRule(new ElementSelector("configuration/statusListener"),  StatusListenerAction::new);

        rs.addRule(new ElementSelector("*/appender"),  AppenderAction::new);
        rs.addRule(new ElementSelector("configuration/appender/appender-ref"),  AppenderRefAction::new);
        rs.addRule(new ElementSelector("configuration/newRule"),  NewRuleAction::new);

        rs.addRule(new ElementSelector("*/param"),  ParamAction::new);

        // add if-then-else support
        rs.addRule(new ElementSelector("*/if"),  IfAction::new);
        rs.addTransparentPathPart("if");
        rs.addRule(new ElementSelector("*/if/then"),  ThenAction::new);
        rs.addTransparentPathPart("then");
        rs.addRule(new ElementSelector("*/if/else"),  ElseAction::new);
        rs.addTransparentPathPart("else");
        
        rs.addRule(new ElementSelector("configuration/appender/sift"),  SiftAction::new);
        rs.addTransparentPathPart("sift");
        
        
    }

    @Override
    protected void setImplicitRuleSupplier(SaxEventInterpreter interpreter) {
        interpreter.setImplicitActionSupplier(  ImplicitModelAction::new );
    }

    @Override
    public void buildModelInterpretationContext() {
        super.buildModelInterpretationContext();
        modelInterpretationContext.createAppenderBags();
    }

    public SaxEventInterpretationContext getInterpretationContext() {
        return saxEventInterpreter.getSaxEventInterpretationContext();
    }

    @Override
    protected void addModelHandlerAssociations(DefaultProcessor defaultProcessor) {
        defaultProcessor.addHandler(ImportModel.class, ImportModelHandler::makeInstance);

        defaultProcessor.addHandler(ShutdownHookModel.class, ShutdownHookModelHandler::makeInstance);
        defaultProcessor.addHandler(EventEvaluatorModel.class, EventEvaluatorModelHandler::makeInstance);
        defaultProcessor.addHandler(DefineModel.class, DefineModelHandler::makeInstance);
        defaultProcessor.addHandler(IncludeModel.class, NOPModelHandler::makeInstance);

        
        defaultProcessor.addHandler(ParamModel.class, ParamModelHandler::makeInstance);
        defaultProcessor.addHandler(PropertyModel.class, PropertyModelHandler::makeInstance);
        defaultProcessor.addHandler(TimestampModel.class, TimestampModelHandler::makeInstance);
        defaultProcessor.addHandler(StatusListenerModel.class, StatusListenerModelHandler::makeInstance);
        defaultProcessor.addHandler(ImplicitModel.class, ImplicitModelHandler::makeInstance);
        
        defaultProcessor.addHandler(IfModel.class, IfModelHandler::makeInstance);
        defaultProcessor.addHandler(ThenModel.class, ThenModelHandler::makeInstance);
        defaultProcessor.addHandler(ElseModel.class, ElseModelHandler::makeInstance);
        
        defaultProcessor.addHandler(SiftModel.class, SiftModelHandler::makeInstance);
        
    }

}
