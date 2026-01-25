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

package ch.qos.logback.core.joran;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.*;
import ch.qos.logback.core.model.conditional.ByPropertiesConditionModel;
import ch.qos.logback.core.model.conditional.ElseModel;
import ch.qos.logback.core.model.conditional.IfModel;
import ch.qos.logback.core.model.conditional.ThenModel;
import ch.qos.logback.core.model.processor.*;
import ch.qos.logback.core.model.processor.conditional.ByPropertiesConditionModelHandler;
import ch.qos.logback.core.model.processor.conditional.ElseModelHandler;
import ch.qos.logback.core.model.processor.conditional.IfModelHandler;
import ch.qos.logback.core.model.processor.conditional.ThenModelHandler;
import ch.qos.logback.core.sift.SiftModelHandler;

/**
 * For a given DefaultProcessor instance link a {@link ch.qos.logback.core.model.Model Model} class to
 * a {@link ch.qos.logback.core.model.processor.ModelHandlerBase ModelHandler} instance in logback-core.
 *
 * <p>Derived classes are likely to add further links.</p>
 *
 * @since 1.3.9/1.4.9
 */
public class ModelClassToModelHandlerLinkerBase {


    protected Context context;

    public ModelClassToModelHandlerLinkerBase(Context context) {
        this.context = context;
    }

    public void link(DefaultProcessor defaultProcessor) {
        defaultProcessor.addHandler(ImportModel.class, ImportModelHandler::makeInstance);

        defaultProcessor.addHandler(ShutdownHookModel.class, ShutdownHookModelHandler::makeInstance);
        defaultProcessor.addHandler(SequenceNumberGeneratorModel.class, SequenceNumberGeneratorModelHandler::makeInstance);
        defaultProcessor.addHandler(SerializeModelModel.class, SerializeModelModelHandler::makeInstance);

        defaultProcessor.addHandler(EventEvaluatorModel.class, EventEvaluatorModelHandler::makeInstance);
        defaultProcessor.addHandler(ConversionRuleModel.class, ConversionRuleModelHandler::makeInstance);

        defaultProcessor.addHandler(DefineModel.class, DefineModelHandler::makeInstance);
        defaultProcessor.addHandler(IncludeModel.class, IncludeModelHandler::makeInstance);


        defaultProcessor.addHandler(ParamModel.class, ParamModelHandler::makeInstance);
        defaultProcessor.addHandler(PropertyModel.class, PropertyModelHandler::makeInstance);
        defaultProcessor.addHandler(TimestampModel.class, TimestampModelHandler::makeInstance);
        defaultProcessor.addHandler(StatusListenerModel.class, StatusListenerModelHandler::makeInstance);
        defaultProcessor.addHandler(ImplicitModel.class, ImplicitModelHandler::makeInstance);


        defaultProcessor.addHandler(ByPropertiesConditionModel.class, ByPropertiesConditionModelHandler::makeInstance);
        defaultProcessor.addHandler(IfModel.class, IfModelHandler::makeInstance);
        defaultProcessor.addHandler(ThenModel.class, ThenModelHandler::makeInstance);
        defaultProcessor.addHandler(ElseModel.class, ElseModelHandler::makeInstance);

        defaultProcessor.addHandler(SiftModel.class, SiftModelHandler::makeInstance);
    }

    // The final filters in the two filter chain are rather crucial.
    // They ensure that only Models attached to the firstPhaseFilter will
    // be handled in the first phase and all models not previously handled
    // in the second phase will be handled in a catch-all fallback case.
    protected void sealModelFilters(DefaultProcessor defaultProcessor) {
        defaultProcessor.getPhaseOneFilter().denyAll();
        defaultProcessor.getPhaseTwoFilter().allowAll();
    }
}
