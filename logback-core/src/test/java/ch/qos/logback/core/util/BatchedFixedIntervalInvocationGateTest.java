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
package ch.qos.logback.core.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link BatchedFixedIntervalInvocationGate}. Time is always injected
 * via {@link BatchedFixedIntervalInvocationGate#isTooSoon(long)}; wall-clock
 * time is not used.
 */
public class BatchedFixedIntervalInvocationGateTest {

    private static final Duration LULL = Duration.buildByMilliseconds(100);

    @Test
    public void allowsBatchSizeSuccessiveCallsThenLulls() {
        int batchSize = 3;
        BatchedFixedIntervalInvocationGate gate = new BatchedFixedIntervalInvocationGate(batchSize, LULL);

        long t = 1_000L;
        for (int i = 0; i < batchSize; i++) {
            assertFalse(gate.isTooSoon(t), "call " + i + " within batch should proceed");
        }

        // batch exhausted → lull until t + LULL
        assertTrue(gate.isTooSoon(t));
        assertTrue(gate.isTooSoon(t + LULL.getMilliseconds() - 1));

        // after lull, a new batch of N is available
        long afterLull = t + LULL.getMilliseconds();
        for (int i = 0; i < batchSize; i++) {
            assertFalse(gate.isTooSoon(afterLull), "call " + i + " after lull should proceed");
        }
        assertTrue(gate.isTooSoon(afterLull));
    }

    @Test
    public void batchSizeOneMatchesFixedIntervalGateShape() {
        BatchedFixedIntervalInvocationGate gate = new BatchedFixedIntervalInvocationGate(1, LULL);

        long t = 200L;
        assertFalse(gate.isTooSoon(t));
        assertTrue(gate.isTooSoon(t));
        assertTrue(gate.isTooSoon(t + LULL.getMilliseconds() - 1));
        assertFalse(gate.isTooSoon(t + LULL.getMilliseconds()));
    }

    @Test
    public void timeUnavailableAlwaysAllows() {
        BatchedFixedIntervalInvocationGate gate = new BatchedFixedIntervalInvocationGate(2,
                Duration.buildByMilliseconds(10));
        assertFalse(gate.isTooSoon(InvocationGate.TIME_UNAVAILABLE));
        assertFalse(gate.isTooSoon(-1));
    }

    @Test
    public void timeAdvancesWithinOpenBatchStillAllowsUntilBatchExhausted() {
        int batchSize = 4;
        BatchedFixedIntervalInvocationGate gate = new BatchedFixedIntervalInvocationGate(batchSize, LULL);

        // Inject increasing timestamps while still inside the batch; all should proceed.
        assertFalse(gate.isTooSoon(10L));
        assertFalse(gate.isTooSoon(11L));
        assertFalse(gate.isTooSoon(50L));
        assertFalse(gate.isTooSoon(99L));
        // 5th call starts lull at 99 + LULL
        assertTrue(gate.isTooSoon(99L));
        assertTrue(gate.isTooSoon(99L + LULL.getMilliseconds() - 1));
        assertFalse(gate.isTooSoon(99L + LULL.getMilliseconds()));
    }

    @Test
    public void multipleBatchesWithInjectedTime() {
        int batchSize = 2;
        Duration lull = Duration.buildByMilliseconds(20);
        BatchedFixedIntervalInvocationGate gate = new BatchedFixedIntervalInvocationGate(batchSize, lull);

        long t = 0L;
        // batch 1
        assertFalse(gate.isTooSoon(t));
        assertFalse(gate.isTooSoon(t));
        assertTrue(gate.isTooSoon(t));

        t = lull.getMilliseconds(); // end of first lull
        // batch 2
        assertFalse(gate.isTooSoon(t));
        assertFalse(gate.isTooSoon(t + 5));
        assertTrue(gate.isTooSoon(t + 5));

        t = t + 5 + lull.getMilliseconds(); // end of second lull
        assertFalse(gate.isTooSoon(t));
    }

    @Test
    public void invalidBatchSizeRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BatchedFixedIntervalInvocationGate(0, LULL));
        assertThrows(IllegalArgumentException.class, () -> new BatchedFixedIntervalInvocationGate(-1, LULL));
    }

    @Test
    public void nullIncrementRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BatchedFixedIntervalInvocationGate(2, null));
    }

    @Test
    public void defaultConstructorIsUsableWithInjectedTime() {
        BatchedFixedIntervalInvocationGate gate = new BatchedFixedIntervalInvocationGate();
        long t = 1L;
        assertFalse(gate.isTooSoon(t));
        // remaining DEFAULT_BATCH_SIZE - 1 still open at same t
        for (int i = 1; i < BatchedFixedIntervalInvocationGate.DEFAULT_BATCH_SIZE; i++) {
            assertFalse(gate.isTooSoon(t));
        }
        assertTrue(gate.isTooSoon(t));
    }
}
