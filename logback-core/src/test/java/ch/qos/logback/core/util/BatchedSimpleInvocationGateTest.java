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
 * Tests for {@link BatchedSimpleInvocationGate}. Time is always injected via
 * {@link BatchedSimpleInvocationGate#isTooSoon(long)}; wall-clock time is not
 * used.
 */
public class BatchedSimpleInvocationGateTest {

    private static final Duration LULL = Duration.buildByMilliseconds(100);

    @Test
    public void allowsBatchSizeSuccessiveCallsThenLulls() {
        int batchSize = 3;
        BatchedSimpleInvocationGate gate = new BatchedSimpleInvocationGate(batchSize, LULL);

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
    public void batchSizeOneMatchesSimpleGateShape() {
        BatchedSimpleInvocationGate gate = new BatchedSimpleInvocationGate(1, LULL);

        long t = 200L;
        assertFalse(gate.isTooSoon(t));
        assertTrue(gate.isTooSoon(t));
        assertTrue(gate.isTooSoon(t + LULL.getMilliseconds() - 1));
        assertFalse(gate.isTooSoon(t + LULL.getMilliseconds()));
    }

    @Test
    public void timeUnavailableAlwaysAllows() {
        BatchedSimpleInvocationGate gate = new BatchedSimpleInvocationGate(2, Duration.buildByMilliseconds(10));
        assertFalse(gate.isTooSoon(InvocationGate.TIME_UNAVAILABLE));
        assertFalse(gate.isTooSoon(-1));
    }

    @Test
    public void timeAdvancesWithinOpenBatchStillAllowsUntilBatchExhausted() {
        int batchSize = 4;
        BatchedSimpleInvocationGate gate = new BatchedSimpleInvocationGate(batchSize, LULL);

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
        BatchedSimpleInvocationGate gate = new BatchedSimpleInvocationGate(batchSize, lull);

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
        assertThrows(IllegalArgumentException.class, () -> new BatchedSimpleInvocationGate(0, LULL));
        assertThrows(IllegalArgumentException.class, () -> new BatchedSimpleInvocationGate(-1, LULL));
    }

    @Test
    public void nullIncrementRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BatchedSimpleInvocationGate(2, null));
    }

    @Test
    public void defaultConstructorIsUsableWithInjectedTime() {
        BatchedSimpleInvocationGate gate = new BatchedSimpleInvocationGate();
        long t = 1L;
        assertFalse(gate.isTooSoon(t));
        // remaining DEFAULT_BATCH_SIZE - 1 still open at same t
        for (int i = 1; i < BatchedSimpleInvocationGate.DEFAULT_BATCH_SIZE; i++) {
            assertFalse(gate.isTooSoon(t));
        }
        assertTrue(gate.isTooSoon(t));
    }
}
