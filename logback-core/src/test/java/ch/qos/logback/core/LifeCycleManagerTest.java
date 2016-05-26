/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.core;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * Unit tests for {@link LifeCycleManager}.
 *
 * @author Carl Harris
 */
public class LifeCycleManagerTest {

    private LifeCycleManager manager = new LifeCycleManager();

    @Test
    public void testRegisterAndReset() {
        MockLifeCycleComponent component = new MockLifeCycleComponent();
        manager.register(component);
        component.start();
        manager.reset();
        assertFalse(component.isStarted());
    }

}
