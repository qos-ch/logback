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
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.SequenceNumberGeneratorModel;
import ch.qos.logback.core.spi.BasicSequenceNumberGenerator;
import ch.qos.logback.core.spi.SequenceNumberGenerator;
import ch.qos.logback.core.util.OptionHelper;

public class SequenceNumberGeneratorModelHandler extends ModelHandlerBase {

    SequenceNumberGenerator sequenceNumberGenerator;
    private boolean inError;

    public SequenceNumberGeneratorModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new SequenceNumberGeneratorModelHandler(context);
    }

    @Override
    protected Class<SequenceNumberGeneratorModel> getSupportedModelClass() {
        return SequenceNumberGeneratorModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {

        SequenceNumberGeneratorModel sequenceNumberGeneratorModel = (SequenceNumberGeneratorModel) model;
        String className = sequenceNumberGeneratorModel.getClassName();
        if (OptionHelper.isNullOrEmpty(className)) {
            addWarn("Missing className. This should have been caught earlier.");
            inError = true;
            return;
        } else {
            className = mic.getImport(className);
        }

        try {
            addInfo("About to instantiate SequenceNumberGenerator of type [" + className + "]");

            sequenceNumberGenerator = (SequenceNumberGenerator) OptionHelper.instantiateByClassName(className,
                    SequenceNumberGenerator.class, context);
            sequenceNumberGenerator.setContext(context);

            mic.pushObject(sequenceNumberGenerator);
        } catch (Exception e) {
            inError = true;
            addError("Could not create a SequenceNumberGenerator of type [" + className + "].", e);
            throw new ModelHandlerException(e);
        }
    }

    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        if (inError) {
            return;
        }

        Object o = mic.peekObject();
        if (o != sequenceNumberGenerator) {
            addWarn("The object at the of the stack is not the hook pushed earlier.");
        } else {
            mic.popObject();

            addInfo("Registering "+o+" with context.");
            context.setSequenceNumberGenerator(sequenceNumberGenerator);
        }
    }

}
