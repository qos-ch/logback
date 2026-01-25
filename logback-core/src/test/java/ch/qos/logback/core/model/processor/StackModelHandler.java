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

package ch.qos.logback.core.model.processor;

import java.util.Stack;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.StackModel;

public class StackModelHandler  extends ModelHandlerBase {

    static public final String STACK_TEST = "STACK_TEST"; 
    
    public StackModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new StackModelHandler(context);
    }

    @Override
    protected Class<StackModel> getSupportedModelClass() {
        return StackModel.class;
    }
    
    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        
        StackModel stackModel = (StackModel) model;
        
        String name = stackModel.getName();
        
        ContextBase contextBase = (ContextBase) context;
        
        @SuppressWarnings("unchecked")
        Stack<String> aStack = (Stack) context.getObject(STACK_TEST);
        if(aStack == null) {
            aStack = new Stack<>();
            contextBase.putObject(STACK_TEST, aStack);
        }
        aStack.push(name);
    }
    
    @Override
    public void postHandle(ModelInterpretationContext intercon, Model model) throws ModelHandlerException {
    }

}
