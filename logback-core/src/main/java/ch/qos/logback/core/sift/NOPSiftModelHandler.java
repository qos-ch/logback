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

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.SiftModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class NOPSiftModelHandler extends ModelHandlerBase {

    public NOPSiftModelHandler(Context context) {
        super(context);
    }

    static public NOPSiftModelHandler makeInstance(Context context, ModelInterpretationContext ic) {
        return new NOPSiftModelHandler(context);
    }

    @Override
    protected Class<SiftModel> getSupportedModelClass() {
        return SiftModel.class;
    }
    
    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
 
    }

}
