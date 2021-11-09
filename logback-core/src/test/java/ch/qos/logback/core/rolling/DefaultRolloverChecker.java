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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.util.Compare;

public class DefaultRolloverChecker implements RolloverChecker {

    final String testId;
    final boolean withCompression;
    final String compressionSuffix;

    public DefaultRolloverChecker(final String testId, final boolean withCompression, final String compressionSuffix) {
        this.testId = testId;
        this.withCompression = withCompression;
        this.compressionSuffix = compressionSuffix;
    }

    @Override
    public void check(final List<String> expectedFilenameList) throws IOException {

        int i = 0;
        for (final String fn : expectedFilenameList) {
            final String suffix = withCompression ? addGZIfNotLast(expectedFilenameList, i, compressionSuffix) : "";

            final String witnessFileName = CoreTestConstants.TEST_SRC_PREFIX + "witness/rolling/tbr-" + testId + "." + i + suffix;
            assertTrue(Compare.compare(fn, witnessFileName));
            i++;
        }
    }

    String addGZIfNotLast(final List<String> expectedFilenameList, final int i, final String suff) {
        final int lastIndex = expectedFilenameList.size() - 1;
        return i != lastIndex ? suff : "";
    }
}
