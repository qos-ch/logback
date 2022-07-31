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
package ch.qos.logback.core.sift;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.processor.DefaultProcessor;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class SiftProcessor<E> extends DefaultProcessor {


    public SiftProcessor(Context context, ModelInterpretationContext mic) {
        super(mic.getContext(), mic);
    }
    
    ModelInterpretationContext getModelInterpretationContext() {
        return mic;
    }

//    final static String ONE_AND_ONLY_ONE_URL = CoreConstants.CODES_URL + "#1andOnly1";

    
//    void foo() {
//        this.modelInterpretationContext = new ModelInterpretationContext(context);
//        buildModelInterpretationContext();
//        DefaultProcessor defaultProcessor = new DefaultProcessor(context, this.modelInterpretationContext);
//        addModelHandlerAssociations(defaultProcessor);
//
//    }


    //abstract public Appender<E> getAppender();

//    int errorEmmissionCount = 0;
//
//    protected void oneAndOnlyOneCheck(Map<?, ?> appenderMap) {
//        String errMsg = null;
//        if (appenderMap.size() == 0) {
//            errorEmmissionCount++;
//            errMsg = "No nested appenders found within the <sift> element in SiftingAppender.";
//        } else if (appenderMap.size() > 1) {
//            errorEmmissionCount++;
//            errMsg = "Only and only one appender can be nested the <sift> element in SiftingAppender. See also "
//                    + ONE_AND_ONLY_ONE_URL;
//        }
//
//        if (errMsg != null && errorEmmissionCount < CoreConstants.MAX_ERROR_COUNT) {
//            addError(errMsg);
//        }
//    }

//    @Override
//    public String toString() {
//        return this.getClass().getName() + "{" + key + "=" + value + '}';
//    }

}
