/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.status.testUtil;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.StatusUtil;
import org.junit.jupiter.api.Assertions;

/**
 * Extend StatusUtil with assertions.
 */
public class StatusChecker extends StatusUtil {

    public StatusChecker(StatusManager sm) {
        super(sm);
    }

    public StatusChecker(Context context) {
        super(context);
    }

    public void assertContainsMatch(int level, String regex) {
        Assertions.assertTrue(containsMatch(level, regex));
    }

    public void assertNoMatch(String regex) {
        Assertions.assertFalse(containsMatch(regex));
    }

    public void assertContainsMatch(String regex) {
        Assertions.assertTrue(containsMatch(regex));
    }

    public void assertContainsException(Class<?> scanExceptionClass) {
        Assertions.assertTrue(containsException(scanExceptionClass));
    }

    public void assertContainsException(Class<?> scanExceptionClass, String msg) {
        Assertions.assertTrue(containsException(scanExceptionClass, msg));
    }
    
    public void assertIsErrorFree() {
        Assertions.assertTrue(isErrorFree(0));
    }

    public void assertIsErrorFree(long treshhold) {
        Assertions.assertTrue(isErrorFree(0));
    }

    public void assertIsWarningOrErrorFree() {
        Assertions.assertTrue(isWarningOrErrorFree(0));
    }

    public void assertErrorCount(int i) {
    }
}
