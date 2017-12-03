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
package ch.qos.logback.core.rolling;

import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.util.Compare;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ZRolloverChecker implements RolloverChecker {

    String testId;

    public ZRolloverChecker(String testId) {
        this.testId = testId;
    }

    public void check(List<String> expectedFilenameList) throws IOException {
        int lastIndex = expectedFilenameList.size() - 1;
        String lastFile = expectedFilenameList.get(lastIndex);
        String witnessFileName = CoreTestConstants.TEST_SRC_PREFIX + "witness/rolling/tbr-" + testId;
        assertTrue(Compare.compare(lastFile, witnessFileName));
    }
}
