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

package ch.qos.logback.core.model.processor.conditional;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.boolex.PropertyCondition;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.conditional.IfModel;
import ch.qos.logback.core.model.conditional.ByPropertiesConditionModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

import static ch.qos.logback.core.model.conditional.IfModel.BranchState.ELSE_BRANCH;
import static ch.qos.logback.core.model.conditional.IfModel.BranchState.IF_BRANCH;

public class ByPropertiesConditionModelHandler extends ModelHandlerBase {

    private boolean inError = false;
    PropertyCondition propertyEvaluator;

    public ByPropertiesConditionModelHandler(Context context) {
        super(context);
    }

    @Override
    protected Class<ByPropertiesConditionModel> getSupportedModelClass() {
        return ByPropertiesConditionModel.class;
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext mic) {
        return new ByPropertiesConditionModelHandler(context);
    }


    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {

        ByPropertiesConditionModel byPropertiesConditionModel = (ByPropertiesConditionModel) model;
        String className = byPropertiesConditionModel.getClassName();
        if (OptionHelper.isNullOrEmptyOrAllSpaces(className)) {
            addWarn("Missing className. This should have been caught earlier.");
            inError = true;
            return;
        } else {
            className = mic.getImport(className);
        }
        try {
            addInfo("About to instantiate PropertyEvaluator of type [" + className + "]");

            propertyEvaluator = (PropertyCondition) OptionHelper.instantiateByClassName(className,
                    PropertyCondition.class, context);
            propertyEvaluator.setContext(context);
            propertyEvaluator.setLocalPropertyContainer(mic);
            mic.pushObject(propertyEvaluator);
        } catch (Exception e) {
            inError = true;
            mic.pushObject(IfModel.BranchState.IN_ERROR);
            addError("Could not create a SequenceNumberGenerator of type [" + className + "].", e);
            throw new ModelHandlerException(e);
        }
    }

    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        if (inError) {
            return;
        }
        Object o = mic.peekObject();
        if (o != propertyEvaluator) {
            addWarn("The object at the of the stack is not the propertyEvaluator instance pushed earlier.");
        } else {
            mic.popObject();
        }

        propertyEvaluator.start();
        if(!propertyEvaluator.isStarted()) {
            addError("PropertyEvaluator of type ["+propertyEvaluator.getClass().getName()+"] did not start successfully.");
            mic.pushObject(IfModel.BranchState.IN_ERROR);
            return;
        }
        boolean evaluationResult = propertyEvaluator.evaluate();
        IfModel.BranchState branchState = evaluationResult ? IF_BRANCH : ELSE_BRANCH;
        mic.pushObject(branchState);

    }
}
