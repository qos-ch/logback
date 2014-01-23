/*
 * Copyright (c) 2014 QOS.ch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QOS.ch - initial API and implementation and/or initial documentation
 */
package ch.qos.logback.core.rolling;

import ch.qos.logback.core.encoder.EchoEncoder;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author tra
 */
public class DeleteOldFilesRollingPolicyTest extends ScaffoldingForRollingTests {

    protected long MS_DAY = 1000L * 60 * 60 * 24;
    DeleteOldFilesRollingPolicy dorp;
    RollingFileAppender rfa;
    TriggeringPolicy tp;

    @Before
    public void before() {
        super.setUp();
        dorp = new DeleteOldFilesRollingPolicy();
        dorp.setContext(context);

        tp = new TriggeringPolicy() {
            private boolean started = false;
            private int calls = 0;

            public boolean isTriggeringEvent(File activeFile, Object event) {
                if (started) {
                    final boolean trigger = calls++ == 0;
                    //System.out.println("CALLS " + calls + " trigger " + trigger);
                    return (trigger);
                } else {
                    return false;
                }
            }

            public void start() {
                started = true;
                calls = 0;
            }

            public void stop() {
                started = false;
            }

            public boolean isStarted() {
                return started;
            }
        };

        rfa = new RollingFileAppender<Object>();

        rfa.setContext(context);

        rfa.setRollingPolicy(dorp);
        rfa.setTriggeringPolicy(tp);

        dorp.setParent(rfa);
    }

    @Test
    public void twoOldFilesOneNew() throws Exception {
        rfa.setFile(randomOutputDir + "/new.log");

        long now = System.currentTimeMillis();
        final String activeFileName = dorp.getActiveFileName();
        File dir = new File(activeFileName).getParentFile();

        Assert.assertFalse(randomOutputDir, dir.exists());
        Assert.assertTrue("mkdir", dir.mkdir());
        Assert.assertTrue(randomOutputDir, dir.exists());

        File old1 = new File(dir, "old1.log");
        Assert.assertTrue("old1", old1.createNewFile());
        old1.setLastModified(now - 10 * MS_DAY);
        File old2 = new File(dir, "old2-long-name.log");
        Assert.assertTrue("old2", old2.createNewFile());
        old2.setLastModified(now - 8 * MS_DAY);

        File new1 = new File(dir, "new1.log");
        Assert.assertTrue("new1", new1.createNewFile());
        new1.setLastModified(now - 6 * MS_DAY);

        Assert.assertEquals("files before", 3, dir.listFiles().length);
        rfa.setEncoder(new EchoEncoder<Object>());
        dorp.setDaysToKeep(7);

        tp.start();
        dorp.start();
        rfa.start();

        rfa.doAppend("e1");

        List<String> expectedFiles = new ArrayList<String>();
        expectedFiles.add("new.log");
        expectedFiles.add("new1.log");

        {
            List<String> f1 = new ArrayList<String>(Arrays.asList(dir.list()));
            Collections.sort(f1);
            Assert.assertEquals("files after: " + f1, expectedFiles, f1);
        }
        rfa.doAppend("e2");

        {
            List<String> f1 = new ArrayList<String>(Arrays.asList(dir.list()));
            Collections.sort(f1);
            Assert.assertEquals("files after: " + f1, expectedFiles, f1);
        }
    }
}
