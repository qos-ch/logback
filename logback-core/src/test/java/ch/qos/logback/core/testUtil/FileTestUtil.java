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
package ch.qos.logback.core.testUtil;

import static org.junit.Assert.assertTrue;

import java.io.File;


/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class FileTestUtil {

    public static void makeTestOutputDir() {
        File target = new File(CoreTestConstants.TARGET_DIR);
        if (target.exists() && target.isDirectory()) {
            File testoutput = new File(CoreTestConstants.OUTPUT_DIR_PREFIX);
            if (!testoutput.exists())
                assertTrue(testoutput.mkdir());
        } else {
            throw new IllegalStateException(CoreTestConstants.TARGET_DIR + " does not exist");
        }
    }
}
