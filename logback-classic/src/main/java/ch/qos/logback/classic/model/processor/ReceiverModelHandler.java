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
package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.model.ReceiverModel;
import ch.qos.logback.classic.net.ReceiverBase;
import ch.qos.logback.classic.net.SocketReceiver;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

/**
 * A Joran {@link ModelHandler} for a {@link SocketReceiver} configuration.
 *
 * @author Carl Harris
 */
public class ReceiverModelHandler extends ModelHandlerBase {

    private ReceiverBase receiver;
    private boolean inError;

    public ReceiverModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new ReceiverModelHandler(context);
    }

    @Override
    protected Class<ReceiverModel> getSupportedModelClass() {
        return ReceiverModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        ReceiverModel receiverModel = (ReceiverModel) model;
        String className = receiverModel.getClassName();

        if (OptionHelper.isNullOrEmpty(className)) {
            addError("Missing class name for receiver. ");
            inError = true;
            return;
        } else {
            className = mic.getImport(className);
        }

        try {
            addInfo("About to instantiate receiver of type [" + className + "]");

            receiver = (ReceiverBase) OptionHelper.instantiateByClassName(className, ReceiverBase.class, context);
            receiver.setContext(context);

            mic.pushObject(receiver);
        } catch (Exception ex) {
            inError = true;
            addError("Could not create a receiver of type [" + className + "].", ex);
            throw new ModelHandlerException(ex);
        }

    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        if (inError) {
            return;
        }

        Object o = mic.peekObject();
        if (o != receiver) {
            addWarn("The object at the of the stack is not the receiver pushed earlier.");
        } else {
            mic.popObject();
            addInfo("Registering receiver with context.");
            mic.getContext().register(receiver);
            receiver.start();

        }
    }
}
