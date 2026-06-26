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

import ch.qos.logback.core.util.EnvUtil;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.conditional.Condition;
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

    public static final String BLACKLISTED_REF_DISALLOWED_MSG = "The 'condition' attribute may not contain blacklisted references.";
    public static final String BLACKLISTED_REF_DISALLOWED_SEE = "See also " + CoreConstants.CODES_URL + "#conditionBlacklisted";

    public static final String UNICODE_DISALLOWED_MSG = "The 'condition' attribute may not contain unicode escape characters.";
    public static final String UNICODE_DISALLOWED_SEE = "See also " + CoreConstants.CODES_URL + "#conditionUnicode";


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
        }
    }


    private void emitDeprecationWarningIfNecessary(String conditionStr) {
        if(!OptionHelper.isNullOrEmptyOrAllSpaces(conditionStr)) {
            addWarn(CONDITION_ATTR_DEPRECATED_MSG);
            addWarn(CONDITION_ATTR_DEPRECATED_SEE);
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
