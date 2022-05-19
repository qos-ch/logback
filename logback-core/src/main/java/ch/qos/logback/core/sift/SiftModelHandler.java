/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2002, QOS.ch. All rights reserved.
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

import java.util.stream.Stream;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.SiftModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class SiftModelHandler extends ModelHandlerBase {
    final static String ONE_AND_ONLY_ONE_URL = CoreConstants.CODES_URL + "#1andOnly1";
    
    public SiftModelHandler(Context context) {
        super(context);
    }

    static public SiftModelHandler makeInstance(Context context, ModelInterpretationContext ic) {
        return new SiftModelHandler(context);
    }

    @Override
    protected Class<SiftModel> getSupportedModelClass() {
        return SiftModel.class;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        
        SiftModel siftModel = (SiftModel) model;
        // don't let the processor handle sub-models
        siftModel.markAsSkipped();
        
        long appenderModelCount = computeAppenderModelCount(siftModel);
        
        if(appenderModelCount == 0) {
            String errMsg = "No nested appenders found within the <sift> element in SiftingAppender.";    
            addError(errMsg);
            return;
        }
        if(appenderModelCount > 1) {
            String errMsg = "Only and only one appender can be nested the <sift> element in SiftingAppender. See also " + ONE_AND_ONLY_ONE_URL;
            addError(errMsg);
            return;
        }
        
        
        Object o = mic.peekObject();
        if (o instanceof SiftingAppenderBase) {
            @SuppressWarnings("rawtypes")
            SiftingAppenderBase sa = (SiftingAppenderBase) o;

            String key = sa.getDiscriminatorKey();
            @SuppressWarnings("rawtypes")
            AppenderFactoryUsingSiftModel afusm = new AppenderFactoryUsingSiftModel(mic, siftModel, key);
         
            sa.setAppenderFactory(afusm);
            
        } else {
            addError("Unexpected object "+ o);
        }
    }

    private long computeAppenderModelCount(SiftModel siftModel) {
        Stream<Model> stream = siftModel.getSubModels().stream();
        long count = stream.filter((Model m) -> m instanceof AppenderModel).count();
        return count;
    }

}
