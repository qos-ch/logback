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
package ch.qos.logback.core.recovery;

import static org.junit.Assert.*;

import org.junit.Test;

public class RecoveryCoordinatorTest {

    long now = System.currentTimeMillis();
    RecoveryCoordinator rc = new RecoveryCoordinator(now);

    @Test
    public void recoveryNotNeededAfterInit() {
        RecoveryCoordinator rc = new RecoveryCoordinator();
        assertTrue(rc.isTooSoon());
    }

    @Test
    public void recoveryNotNeededIfAsleepForLessThanBackOffTime() throws InterruptedException {
        rc.setCurrentTime(now + RecoveryCoordinator.BACKOFF_COEFFICIENT_MIN / 2);
        assertTrue(rc.isTooSoon());
    }

    @Test
    public void recoveryNeededIfAsleepForMoreThanBackOffTime() throws InterruptedException {
        rc.setCurrentTime(now + RecoveryCoordinator.BACKOFF_COEFFICIENT_MIN + 20);
        assertFalse(rc.isTooSoon());
    }

    @Test
    public void recoveryNotNeededIfCurrentTimeSetToBackOffTime() throws InterruptedException {
        rc.setCurrentTime(now + RecoveryCoordinator.BACKOFF_COEFFICIENT_MIN);
        assertTrue(rc.isTooSoon());
    }

    @Test
    public void recoveryNeededIfCurrentTimeSetToExceedBackOffTime() {
        rc.setCurrentTime(now + RecoveryCoordinator.BACKOFF_COEFFICIENT_MIN + 1);
        assertFalse(rc.isTooSoon());
    }

    @Test
    public void recoveryConditionDetectedEvenAfterReallyLongTimesBetweenRecovery() {
        // Since backoff time quadruples whenever recovery is needed,
        // we double the offset on each for-loop iteration, causing
        // every other iteration to trigger recovery.

        long offset = RecoveryCoordinator.BACKOFF_COEFFICIENT_MIN;

        for (int i = 0; i < 16; i++) {
            rc.setCurrentTime(now + offset);

            if (i % 2 == 0) {
                assertTrue("recovery should've been needed at " + offset, rc.isTooSoon());
            } else {
                assertFalse("recovery should NOT have been needed at " + offset, rc.isTooSoon());
            }
            offset *= 2;
        }
    }
}
