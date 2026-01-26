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

package ch.qos.logback.classic.model;

import ch.qos.logback.core.model.ImportModel;
import ch.qos.logback.core.model.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelDuplicationTest {

    @Test
    public void smoke() {
        ConfigurationModel cm = new ConfigurationModel();
        cm.setDebugStr("x");
        Model copy = Model.duplicate(cm);
        assertEquals(cm, copy);
    }

    @Test
    public void test() {
        ConfigurationModel cm = new ConfigurationModel();
        cm.setDebugStr("x");
        
        ImportModel importModel = new ImportModel();
        importModel.setClassName("a");

        cm.addSubModel(importModel);
        
        Model copy = Model.duplicate(cm);
        assertEquals(cm, copy);
    }
    
    
    
}
