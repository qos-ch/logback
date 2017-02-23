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

import ch.qos.logback.core.util.Compare;
import ch.qos.logback.core.util.CoreTestConstants;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class DefaultRolloverChecker implements RolloverChecker {

    final String testId;
    final boolean withCompression;
    final String compressionSuffix;

    public DefaultRolloverChecker(String testId, boolean withCompression, String compressionSuffix) {
        this.testId = testId;
        this.withCompression = withCompression;
        this.compressionSuffix = compressionSuffix;
    }

    public void check(List<String> expectedFilenameList) throws IOException {

        int i = 0;
        for (String fn : expectedFilenameList) {
            String suffix = withCompression ? addGZIfNotLast(expectedFilenameList, i, compressionSuffix) : "";

            String witnessFileName = CoreTestConstants.TEST_SRC_PREFIX + "witness/rolling/tbr-" + testId + "." + i + suffix;
            assertTrue(Compare.compare(fn, witnessFileName));
            i++;
        }
    }

    String addGZIfNotLast(List<String> expectedFilenameList, int i, String suff) {
        int lastIndex = expectedFilenameList.size() - 1;
        return (i != lastIndex) ? suff : "";
    }
}
