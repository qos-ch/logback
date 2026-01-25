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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.joran.action.ActionUtil.Scope;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ModelConstants;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.model.util.PropertyModelHandlerHelper;
import ch.qos.logback.core.util.Loader;

public class PropertyModelHandler extends ModelHandlerBase {

    public PropertyModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new PropertyModelHandler(context);
    }

    @Override
    protected Class<PropertyModel> getSupportedModelClass() {
        return PropertyModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) {

        PropertyModel propertyModel = (PropertyModel) model;
        PropertyModelHandlerHelper propertyModelHandlerHelper = new PropertyModelHandlerHelper(this);
        propertyModelHandlerHelper.setContext(context);
        propertyModelHandlerHelper.handlePropertyModel(mic, propertyModel);
    }

}
