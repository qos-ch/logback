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
        
        if (!EnvUtil.isJaninoAvailable()) {
            addError(MISSING_JANINO_MSG);
            addError(MISSING_JANINO_SEE);
            return;
        }
        
        mic.pushModel(ifModel);
        Condition condition = null;
        int lineNum = model.getLineNumber();

        String conditionStr = ifModel.getCondition();
        if (!OptionHelper.isNullOrEmpty(conditionStr)) {
            try {
                conditionStr = OptionHelper.substVars(conditionStr, mic, context);
            } catch (ScanException e) {
               addError("Failed to parse input [" + conditionStr + "] on line "+lineNum, e);
               ifModel.setBranchState(BranchState.IN_ERROR);
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
