package ch.qos.logback.core.util;

import static ch.qos.logback.core.util.DefaultInvocationGate.DEFAULT_MASK;
import static ch.qos.logback.core.util.DefaultInvocationGate.MASK_DECREASE_RIGHT_SHIFT_COUNT;
import static org.junit.Assert.*;

import org.junit.Test;

public class DefaultInvocationGateTest {

    @Test
    public void smoke() {
        long currentTime = 0;
        long minDelayThreshold = 4;
        long maxDelayThreshold = 8;

        DefaultInvocationGate gate = new DefaultInvocationGate(minDelayThreshold, maxDelayThreshold, currentTime);
        assertTrue(gate.isTooSoon(0));
    }

    @Test
    public void closelyRepeatedCallsShouldCauseMaskToIncrease() {
        long currentTime = 0;
        long minDelayThreshold = 4;
        long maxDelayThreshold = 8;

        DefaultInvocationGate gate = new DefaultInvocationGate(minDelayThreshold, maxDelayThreshold, currentTime);
        for (int i = 0; i < DEFAULT_MASK; i++) {
            assertTrue(gate.isTooSoon(0));
        }
        assertFalse(gate.isTooSoon(0));
        assertTrue(gate.getMask() > DEFAULT_MASK);
    }

    @Test
    public void stableAtSteadyRate() {
        long currentTime = 0;
        long minDelayThreshold = DEFAULT_MASK;
        long maxDelayThreshold = DEFAULT_MASK * 2;

        DefaultInvocationGate gate = new DefaultInvocationGate(minDelayThreshold, maxDelayThreshold, currentTime);

        for (int t = 0; t < 4 * minDelayThreshold; t++) {
            gate.isTooSoon(currentTime++);
            assertEquals(DEFAULT_MASK, gate.getMask());
        }
    }

    @Test
    public void intermittentCallsShouldCauseMaskToDecrease() {
        long currentTime = 0;
        long minDelayThreshold = 4;
        long maxDelayThreshold = 8;

        DefaultInvocationGate gate = new DefaultInvocationGate(minDelayThreshold, maxDelayThreshold, currentTime);
        int currentMask = DEFAULT_MASK;

        currentTime += maxDelayThreshold + 1;
        assertFalse(gate.isTooSoon(currentTime));
        assertTrue(gate.getMask() < currentMask);
    }

    @Test
    public void maskCanDropToZeroForInfrequentInvocations() {
        long currentTime = 0;
        long minDelayThreshold = 4;
        long maxDelayThreshold = 8;

        DefaultInvocationGate gate = new DefaultInvocationGate(minDelayThreshold, maxDelayThreshold, currentTime);
        int currentMask = DEFAULT_MASK;

        do {
            currentTime += maxDelayThreshold + 1;
            assertFalse(gate.isTooSoon(currentTime));
            assertTrue(gate.getMask() < currentMask);
            currentMask = currentMask >> MASK_DECREASE_RIGHT_SHIFT_COUNT;
        } while (currentMask > 0);

        assertEquals(0, gate.getMask());
        assertFalse(gate.isTooSoon(currentTime));
    }
}
