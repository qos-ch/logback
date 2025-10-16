/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2025, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.model.processor.conditional;

import ch.qos.logback.core.util.EnvUtil;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.conditional.Condition;
import ch.qos.logback.core.joran.conditional.PropertyEvalScriptBuilder;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.conditional.IfModel;
import ch.qos.logback.core.model.conditional.IfModel.BranchState;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.ScanException;

public class IfModelHandler extends ModelHandlerBase {


    public static final String MISSING_JANINO_MSG = "Could not find Janino library on the class path. Skipping conditional processing.";
    public static final String MISSING_JANINO_SEE = "See also " + CoreConstants.CODES_URL + "#ifJanino";

    public static final String NEW_OPERATOR_DISALLOWED_MSG = "The 'condition' attribute may not contain the 'new' operator.";
    public static final String NEW_OPERATOR_DISALLOWED_SEE = "See also " + CoreConstants.CODES_URL + "#conditionNew";

    public static final String CONDITION_ATTR_DEPRECATED_MSG = "The 'condition' attribute in <if> element is deprecated and slated for removal. Use <condition> element instead.";
    public static final String CONDITION_ATTR_DEPRECATED_SEE = "See also " + CoreConstants.CODES_URL + "#conditionAttributeDeprecation";

    enum Branch {IF_BRANCH, ELSE_BRANCH; }
    
    IfModel ifModel = null;
    
    public IfModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new IfModelHandler(context);
    }

    @Override
    protected Class<IfModel> getSupportedModelClass() {
        return IfModel.class;
    }
    
    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        
        ifModel = (IfModel) model;
        mic.pushModel(ifModel);
        Object micTopObject = mic.peekObject();
        String conditionStr = ifModel.getCondition();
        emitDeprecationWarningIfNecessary(conditionStr);


        if(micTopObject instanceof BranchState) {
            BranchState branchState = (BranchState) micTopObject;
            ifModel.setBranchState(branchState);
            // consume the BranchState at top of the object stack
            mic.popObject();
        } else {
            janinoFallback(mic, model, conditionStr);
        }
    }

    private void janinoFallback(ModelInterpretationContext mic, Model model, String conditionStr) {
        if (!EnvUtil.isJaninoAvailable()) {
            addError(MISSING_JANINO_MSG);
            addError(MISSING_JANINO_SEE);
            return;
        }

        Condition condition = null;
        int lineNum = model.getLineNumber();

        if (!OptionHelper.isNullOrEmptyOrAllSpaces(conditionStr)) {
            try {
                conditionStr = OptionHelper.substVars(conditionStr, mic, context);
            } catch (ScanException e) {
               addError("Failed to parse input [" + conditionStr + "] on line "+lineNum, e);
               ifModel.setBranchState(BranchState.IN_ERROR);
                return;
            }

            // do not allow 'new' operator
            if(hasNew(conditionStr)) {
                addError(NEW_OPERATOR_DISALLOWED_MSG);
                addError(NEW_OPERATOR_DISALLOWED_SEE);
                return;
            }

            try {
                PropertyEvalScriptBuilder pesb = new PropertyEvalScriptBuilder(mic);
                pesb.setContext(context);
                condition = pesb.build(conditionStr);
            } catch (Exception|NoClassDefFoundError e) {
                ifModel.setBranchState(BranchState.IN_ERROR);
                addError("Failed to parse condition [" + conditionStr + "] on line "+lineNum, e);
                return;
            }

            if (condition != null) {
                boolean boolResult = condition.evaluate();
                addInfo("Condition ["+conditionStr+"] evaluated to "+boolResult+ " on line "+lineNum);
                ifModel.setBranchState(boolResult);
            } else {
                addError("The condition variable is null. This should not occur.");
                ifModel.setBranchState(BranchState.IN_ERROR);
                return;
            }
        }
    }

    private void emitDeprecationWarningIfNecessary(String conditionStr) {
        if(!OptionHelper.isNullOrEmptyOrAllSpaces(conditionStr)) {
            addWarn(CONDITION_ATTR_DEPRECATED_MSG);
            addWarn(CONDITION_ATTR_DEPRECATED_SEE);
        }
    }


    private boolean hasNew(String conditionStr) {
        return conditionStr.contains("new ");
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {

        if(mic.isModelStackEmpty()) {
            addError("Unexpected unexpected empty model stack.");
            return;
        }

        Object o = mic.peekModel();
        if (o != ifModel) {
            addWarn("The object [" + o + "] on the top the of the stack is not the expected [" + ifModel);
        } else {
            mic.popModel();
        }
    }

}
