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
package ch.qos.logback.core.spi;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * A
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class ScenarioBasedCyclicBufferTrackerTest {

    CyclicBufferTrackerSimulator simulator;
    CyclicBufferTrackerSimulator.Parameters parameters = new CyclicBufferTrackerSimulator.Parameters();

    void verify() {
        CyclicBufferTracker<Object> at = simulator.realCBTracker;
        CyclicBufferTrackerT<Object> t_at = simulator.t_CBTracker;
        assertEquals(t_at.liveKeysAsOrderedList(), at.liveKeysAsOrderedList());
        assertEquals(t_at.lingererKeysAsOrderedList(), at.lingererKeysAsOrderedList());
    }

    @Before
    public void setUp() {
        parameters.keySpaceLen = 128;
        parameters.maxTimestampInc = ComponentTracker.DEFAULT_TIMEOUT / 2;
    }

    @Test
    public void shortTest() {
        parameters.keySpaceLen = 64;
        parameters.maxTimestampInc = 500;
        parameters.simulationLength = 70;

        simulator = new CyclicBufferTrackerSimulator(parameters);
        simulator.buildScenario();
        simulator.simulate();
        verify();
    }

    @Test
    public void mediumTest() {
        parameters.simulationLength = 20000;

        simulator = new CyclicBufferTrackerSimulator(parameters);
        simulator.buildScenario();
        simulator.simulate();
        verify();
    }

    @Test
    public void longTest() {
        parameters.simulationLength = 100 * 1000;
        simulator = new CyclicBufferTrackerSimulator(parameters);
        simulator.buildScenario();
        simulator.simulate();
        verify();
    }
}
