/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 * or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.turbo;

import ch.qos.logback.core.util.Duration;
import junit.framework.Assert;
import org.junit.Test;

public class ExpiringLRUMessageCacheTest {

    @Test
    public void testEldestEntriesRemoval() {
        final ExpiringLRUMessageCache cache = new ExpiringLRUMessageCache(2, new Duration(Integer.MAX_VALUE));
        Assert.assertEquals(0, cache.getMessageCountAndThenIncrement("0"));
        Assert.assertEquals(1, cache.getMessageCountAndThenIncrement("0"));
        Assert.assertEquals(0, cache.getMessageCountAndThenIncrement("1"));
        Assert.assertEquals(1, cache.getMessageCountAndThenIncrement("1"));
        // 0 entry should have been removed.
        Assert.assertEquals(0, cache.getMessageCountAndThenIncrement("2"));
        // So it is expected a returned value of 0 instead of 2.
        // 1 entry should have been removed.
        Assert.assertEquals(0, cache.getMessageCountAndThenIncrement("0"));
        // So it is expected a returned value of 0 instead of 2.
        // 2 entry should have been removed.
        Assert.assertEquals(0, cache.getMessageCountAndThenIncrement("1"));
        // So it is expected a returned value of 0 instead of 2.
        Assert.assertEquals(0, cache.getMessageCountAndThenIncrement("2"));
    }

    /**
     * This test may fail if not processed fast enough, because it is based on elapsed time.
     * Increase times to make it more robust.
     */
    @Test
    public void testExpireEntry() throws InterruptedException {
        final ExpiringLRUMessageCache cache = new ExpiringLRUMessageCache(10, new Duration(100));
        cache.setCurrentTime(0);
        Assert.assertEquals(0, cache.getMessageCountAndThenIncrement("0"));
        Assert.assertEquals(0, cache.getMessageCountAndThenIncrement("1"));
        Assert.assertEquals(1, cache.getMessageCountAndThenIncrement("0"));
        Assert.assertEquals(1, cache.getMessageCountAndThenIncrement("1"));
        cache.setCurrentTime(50);
        Assert.assertEquals(2, cache.getMessageCountAndThenIncrement("0"));
        Assert.assertEquals(2, cache.getMessageCountAndThenIncrement("1"));

        cache.setCurrentTime(160);

        Assert.assertEquals(0, cache.getMessageCountAndThenIncrement("0"));
        Assert.assertEquals(0, cache.getMessageCountAndThenIncrement("1"));
    }

}
