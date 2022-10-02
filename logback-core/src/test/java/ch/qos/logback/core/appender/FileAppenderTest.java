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
package ch.qos.logback.core.appender;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.DummyEncoder;
import ch.qos.logback.core.encoder.NopEncoder;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;

public class FileAppenderTest extends AbstractAppenderTest<Object> {

    int diff = RandomUtil.getPositiveInt();

    protected Appender<Object> getAppender() {
        return new FileAppender<Object>();
    }

    protected Appender<Object> getConfiguredAppender() {
        FileAppender<Object> appender = new FileAppender<Object>();
        appender.setEncoder(new NopEncoder<Object>());
        appender.setFile(CoreTestConstants.OUTPUT_DIR_PREFIX + "temp.log");
        appender.setName("test");
        appender.setContext(context);
        appender.start();
        return appender;
    }

    @Test
    public void smoke() {
        String filename = CoreTestConstants.OUTPUT_DIR_PREFIX + "/fat-smoke.log";

        FileAppender<Object> appender = new FileAppender<Object>();
        appender.setEncoder(new DummyEncoder<Object>());
        appender.setAppend(false);
        appender.setFile(filename);
        appender.setName("smoke");
        appender.setContext(context);
        appender.start();
        appender.doAppend(new Object());
        appender.stop();

        File file = new File(filename);
        Assertions.assertTrue(file.exists());
        Assertions.assertTrue(file.delete(), "failed to delete " + file.getAbsolutePath());
    }

    @Test
    public void testCreateParentFolders() {
        String filename = CoreTestConstants.OUTPUT_DIR_PREFIX + "/fat-testCreateParentFolders-" + diff
                + "/testCreateParentFolders.txt";
        File file = new File(filename);
        Assertions.assertFalse(file.getParentFile().exists());
        Assertions.assertFalse(file.exists());

        FileAppender<Object> appender = new FileAppender<Object>();
        appender.setEncoder(new DummyEncoder<Object>());
        appender.setAppend(false);
        appender.setFile(filename);
        appender.setName("testCreateParentFolders");
        appender.setContext(context);
        appender.start();
        appender.doAppend(new Object());
        appender.stop();
        Assertions.assertTrue(file.getParentFile().exists());
        Assertions.assertTrue(file.exists());

        // cleanup
        Assertions.assertTrue(file.delete(), "failed to delete " + file.getAbsolutePath());
        File parent = file.getParentFile();
        Assertions.assertTrue(parent.delete(), "failed to delete " + parent.getAbsolutePath());
    }

    @Test
    public void testPrudentModeLogicalImplications() {
        String filename = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "fat-testPrudentModeLogicalImplications.txt";
        File file = new File(filename);
        FileAppender<Object> appender = new FileAppender<Object>();
        appender.setEncoder(new DummyEncoder<Object>());
        appender.setFile(filename);
        appender.setName("testPrudentModeLogicalImplications");
        appender.setContext(context);

        appender.setAppend(false);
        appender.setPrudent(true);
        appender.start();

        Assertions.assertTrue(appender.isAppend());

        StatusManager sm = context.getStatusManager();
        // StatusPrinter.print(context);
        StatusChecker statusChecker = new StatusChecker(context);
        Assertions.assertEquals(Status.WARN, statusChecker.getHighestLevel(0));
        List<Status> statusList = sm.getCopyOfStatusList();
        Assertions.assertTrue(
                statusList.size() >= 2, "Expecting status list size to be 2 or larger, but was " + statusList.size());
        String msg1 = statusList.get(1).getMessage();

        Assertions.assertTrue(msg1.startsWith("Setting \"Append\" property"), "Got message [" + msg1 + "]");

        appender.doAppend(new Object());
        appender.stop();
        Assertions.assertTrue(file.exists());
        Assertions.assertTrue(file.delete(), "failed to delete " + file.getAbsolutePath());
    }

    @Test
    public void fileNameCollision() {
        String fileName = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "fileNameCollision";

        FileAppender<Object> appender0 = new FileAppender<Object>();
        appender0.setName("FA0");
        appender0.setFile(fileName);
        appender0.setContext(context);
        appender0.setEncoder(new DummyEncoder<Object>());
        appender0.start();
        Assertions.assertTrue(appender0.isStarted());

        FileAppender<Object> appender1 = new FileAppender<Object>();
        appender1.setName("FA1");
        appender1.setFile(fileName);
        appender1.setContext(context);
        appender1.setEncoder(new DummyEncoder<Object>());
        appender1.start();

        Assertions.assertFalse(appender1.isStarted());

        StatusPrinter.print(context);
        StatusChecker checker = new StatusChecker(context);
        checker.assertContainsMatch(Status.ERROR, "'File' option has the same value");

    }
}
