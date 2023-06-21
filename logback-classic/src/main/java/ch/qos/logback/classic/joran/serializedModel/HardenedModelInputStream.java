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

package ch.qos.logback.classic.joran.serializedModel;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.net.HardenedObjectInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HardenedModelInputStream extends HardenedObjectInputStream {


    static public List<String> getWhilelist() {
        List<String> whitelist = new ArrayList<String>();
        whitelist.add(Model.class.getName());
        whitelist.add(ch.qos.logback.core.model.Model.class.getName());
        whitelist.add(ch.qos.logback.core.model.IncludeModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.InsertFromJNDIModel.class.getName());
        whitelist.add(ch.qos.logback.classic.model.RootLoggerModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.ImportModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.AppenderRefModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.ComponentModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.StatusListenerModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.ShutdownHookModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.NamedComponentModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.AppenderModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.EventEvaluatorModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.DefineModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.SequenceNumberGeneratorModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.ImplicitModel.class.getName());
        whitelist.add(ch.qos.logback.classic.model.ReceiverModel.class.getName());
        whitelist.add(ch.qos.logback.classic.model.LoggerContextListenerModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.conditional.ThenModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.conditional.IfModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.NamedModel.class.getName());
        whitelist.add(ch.qos.logback.classic.model.ContextNameModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.ParamModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.TimestampModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.PropertyModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.conditional.ElseModel.class.getName());
        whitelist.add(ch.qos.logback.classic.model.ConfigurationModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.SiftModel.class.getName());
        whitelist.add(ch.qos.logback.classic.model.LoggerModel.class.getName());
        whitelist.add(ch.qos.logback.core.model.SerializeModelModel.class.getName());


        return whitelist;
    }
    public HardenedModelInputStream(InputStream is) throws IOException {
        super(is, getWhilelist());
    }
}
