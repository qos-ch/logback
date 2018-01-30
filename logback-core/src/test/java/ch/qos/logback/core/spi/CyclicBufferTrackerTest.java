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

import ch.qos.logback.core.helpers.CyclicBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;


/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class CyclicBufferTrackerTest {

    CyclicBufferTracker<Object> tracker = new CyclicBufferTracker<Object>();
    String key = "a";

    @Test
    public void empty0() {
        long now = 3000;
        tracker.removeStaleComponents(now);
        assertEquals(0, tracker.liveKeysAsOrderedList().size());
        assertEquals(0, tracker.getComponentCount());
    }

    @Test
    public void empty1() {
        long now = 3000;
        assertNotNull(tracker.getOrCreate(key, now++));
        now += ComponentTracker.DEFAULT_TIMEOUT + 1000;
        tracker.removeStaleComponents(now);
        assertEquals(0, tracker.liveKeysAsOrderedList().size());
        assertEquals(0, tracker.getComponentCount());

        assertNotNull(tracker.getOrCreate(key, now++));
    }

    @Test
    public void smoke() {
        long now = 3000;
        CyclicBuffer<Object> cb = tracker.getOrCreate(key, now);
        assertEquals(cb, tracker.getOrCreate(key, now++));
        now += CyclicBufferTracker.DEFAULT_TIMEOUT + 1000;
        tracker.removeStaleComponents(now);
        assertEquals(0, tracker.liveKeysAsOrderedList().size());
        assertEquals(0, tracker.getComponentCount());
    }

    @Test
    public void destroy() {
        long now = 3000;
        CyclicBuffer<Object> cb = tracker.getOrCreate(key, now);
        cb.add(new Object());
        assertEquals(1, cb.length());
        tracker.endOfLife(key);
        now += CyclicBufferTracker.LINGERING_TIMEOUT + 10;
        tracker.removeStaleComponents(now);
        assertEquals(0, tracker.liveKeysAsOrderedList().size());
        assertEquals(0, tracker.getComponentCount());
        assertEquals(0, cb.length());
    }

}
