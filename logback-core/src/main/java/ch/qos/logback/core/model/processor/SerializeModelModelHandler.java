/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2023, QOS.ch. All rights reserved.
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
import ch.qos.logback.core.model.SerializeModelModel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static ch.qos.logback.core.CoreConstants.FILE_TIMESTAMP_PATTERN;
import static ch.qos.logback.core.CoreConstants.MODEL_CONFIG_FILE_EXTENSION;

public class SerializeModelModelHandler extends ModelHandlerBase {

    public SerializeModelModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext mic) {
        return new SerializeModelModelHandler(context);
    }

    @Override
    public void handle(ModelInterpretationContext modelInterpretationContext, Model model)
            throws ModelHandlerException {


        Object configuratorHint = modelInterpretationContext.getConfiguratorHint();

        if(configuratorHint != null && configuratorHint.getClass().getName().equals("ch.qos.logback.classic.joran.SerializedModelConfigurator")) {
            addWarn("Skipping model serialization as calling configurator is model based.");
            return;
        }

        if (!(model instanceof SerializeModelModel)) {
            addWarn("Model parameter is not of type SerializeModelModel. Skipping serialization of model structure");
            return;
        }

        SerializeModelModel serializeModelModel = (SerializeModelModel) model;

        Model topModel = modelInterpretationContext.getTopModel();

        if (topModel == null) {
            addWarn("Could not find top most model. Skipping serialization of model structure.");
            return;
        }

        String fileStr = serializeModelModel.getFile();
        if (fileStr == null) {
            DateTimeFormatter dft = DateTimeFormatter.ofPattern(FILE_TIMESTAMP_PATTERN);
            Instant now = Instant.now();
            String timestamp = dft.format(now);
            fileStr = "logback-" + timestamp + MODEL_CONFIG_FILE_EXTENSION;
            addInfo("For model serialization, using default file destination [" + fileStr + "]");
        } else {
            fileStr = modelInterpretationContext.subst(fileStr);
        }

        writeModel(fileStr, topModel);
    }

    private void writeModel(String fileStr, Model firstModel) {

        addInfo("Serializing model to file ["+fileStr+"]");

        try (FileOutputStream fos = new FileOutputStream(fileStr)) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(firstModel);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            addError("IO failure while serializing Model ["+fileStr+"]");
        }
    }
}
