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
package ch.qos.logback.core.helpers;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.rolling.helper.FileFilterUtil;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import org.junit.Test;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static junit.framework.Assert.assertEquals;

public class FileFilterUtilTest {

    Context context = new ContextBase();

    // see also http://jira.qos.ch/browse/LBCORE-164
    @Test
    public void findHighestCounterTest() throws ParseException {
        String[] sa = new String[] { "c:/log/debug-old-2010-08-10.0.log", "c:/log/debug-old-2010-08-10.1.log", "c:/log/debug-old-2010-08-10.10.log",
                "c:/log/debug-old-2010-08-10.11.log", "c:/log/debug-old-2010-08-10.12.log", "c:/log/debug-old-2010-08-10.2.log",
                "c:/log/debug-old-2010-08-10.3.log", "c:/log/debug-old-2010-08-10.4.log", "c:/log/debug-old-2010-08-10.5.log",
                "c:/log/debug-old-2010-08-10.6.log", "c:/log/debug-old-2010-08-10.7.log", "c:/log/debug-old-2010-08-10.8.log",
                "c:/log/debug-old-2010-08-10.9.log" };

        File[] matchingFileArray = new File[sa.length];
        for (int i = 0; i < sa.length; i++) {
            matchingFileArray[i] = new File(sa[i]);
        }
        FileNamePattern fnp = new FileNamePattern("c:/log/debug-old-%d{yyyy-MM-dd}.%i.log", context);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String rexexp = null;
        rexexp = fnp.toRegexForFixedDate(sdf.parse("2010-08-10"));
        String stemRegex = FileFilterUtil.afterLastSlash(rexexp);
        int result = FileFilterUtil.findHighestCounter(matchingFileArray, stemRegex);
        assertEquals(12, result);
    }
}
