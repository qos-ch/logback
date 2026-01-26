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

package ch.qos.logback.classic.joran.serializedModel;

import ch.qos.logback.classic.model.ConfigurationModel;
import ch.qos.logback.classic.model.LoggerModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.net.HardenedObjectInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelSerializationTest {


    ByteArrayOutputStream bos;
    ObjectOutputStream oos;
    HardenedObjectInputStream inputStream;
    //String[] whitelist = new String[] {  };


    @BeforeEach
    public void setUp() throws Exception {
        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void smoke() throws ClassNotFoundException, IOException {
        ConfigurationModel configurationModel = new ConfigurationModel();
        configurationModel.setTag("configuration");
        configurationModel.setDebugStr("true");

        LoggerModel loggerModel = new LoggerModel();
        loggerModel.setTag("logger");
        loggerModel.setLevel("DEBUG");
        configurationModel.addSubModel(loggerModel);

        Model back = writeAndRead(configurationModel);
        assertEquals(configurationModel, back);
    }

    private Model writeAndRead(Model model) throws IOException, ClassNotFoundException {
        writeObject(oos, model);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        inputStream = new HardenedModelInputStream(bis);
        Model fooBack = (Model) inputStream.readObject();
        inputStream.close();
        return fooBack;
    }

    private void writeObject(ObjectOutputStream oos, Object o) throws IOException {
        oos.writeObject(o);
        oos.flush();
        oos.close();
    }
}
