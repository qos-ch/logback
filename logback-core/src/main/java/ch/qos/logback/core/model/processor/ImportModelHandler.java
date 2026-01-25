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

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.model.ImportModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.util.OptionHelper;

public class ImportModelHandler extends ModelHandlerBase {

    public ImportModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new ImportModelHandler(context);
    }

    @Override
    protected Class<ImportModel> getSupportedModelClass() {
        return ImportModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext intercon, Model model) throws ModelHandlerException {
        ImportModel importModel = (ImportModel) model;

        String className = importModel.getClassName();
        if (OptionHelper.isNullOrEmptyOrAllSpaces(className)) {
            addWarn("Empty className not allowed");
            return;
        }

        String stem = extractStem(className);
        if (stem == null) {
            addWarn("[" + className + "] could not be imported due to incorrect format");
            return;
        }

        intercon.addImport(stem, className);

    }

    String extractStem(String className) {
        if (className == null)
            return null;

        int lastDotIndex = className.lastIndexOf(CoreConstants.DOT);
        if (lastDotIndex == -1)
            return null;
        if ((lastDotIndex + 1) == className.length())
            return null;
        return className.substring(lastDotIndex + 1);
    }

}
