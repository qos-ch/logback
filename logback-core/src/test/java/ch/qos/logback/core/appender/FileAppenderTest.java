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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.DummyEncoder;
import ch.qos.logback.core.encoder.NopEncoder;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.testUtil.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;

public class FileAppenderTest extends AbstractAppenderTest<Object> {

    int diff = RandomUtil.getPositiveInt();

    @Override
    protected Appender<Object> getAppender() {
        return new FileAppender<>();
    }

    @Override
    protected Appender<Object> getConfiguredAppender() {
        final FileAppender<Object> appender = new FileAppender<>();
        appender.setEncoder(new NopEncoder<>());
        appender.setFile(CoreTestConstants.OUTPUT_DIR_PREFIX + "temp.log");
        appender.setName("test");
        appender.setContext(context);
        appender.start();
        return appender;
    }

    @Test
    public void smoke() {
        final String filename = CoreTestConstants.OUTPUT_DIR_PREFIX + "/fat-smoke.log";

        final FileAppender<Object> appender = new FileAppender<>();
        appender.setEncoder(new DummyEncoder<>());
        appender.setAppend(false);
        appender.setFile(filename);
        appender.setName("smoke");
        appender.setContext(context);
        appender.start();
        appender.doAppend(new Object());
        appender.stop();

        final File file = new File(filename);
        assertTrue(file.exists());
        assertTrue("failed to delete " + file.getAbsolutePath(), file.delete());
    }

    @Test
    public void testCreateParentFolders() {
        final String filename = CoreTestConstants.OUTPUT_DIR_PREFIX + "/fat-testCreateParentFolders-" + diff + "/testCreateParentFolders.txt";
        final File file = new File(filename);
        assertFalse(file.getParentFile().exists());
        assertFalse(file.exists());

        final FileAppender<Object> appender = new FileAppender<>();
        appender.setEncoder(new DummyEncoder<>());
        appender.setAppend(false);
        appender.setFile(filename);
        appender.setName("testCreateParentFolders");
        appender.setContext(context);
        appender.start();
        appender.doAppend(new Object());
        appender.stop();
        assertTrue(file.getParentFile().exists());
        assertTrue(file.exists());

        // cleanup
        assertTrue("failed to delete " + file.getAbsolutePath(), file.delete());
        final File parent = file.getParentFile();
        assertTrue("failed to delete " + parent.getAbsolutePath(), parent.delete());
    }

    @Test
    public void testPrudentModeLogicalImplications() {
        final String filename = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "fat-testPrudentModeLogicalImplications.txt";
        final File file = new File(filename);
        final FileAppender<Object> appender = new FileAppender<>();
        appender.setEncoder(new DummyEncoder<>());
        appender.setFile(filename);
        appender.setName("testPrudentModeLogicalImplications");
        appender.setContext(context);

        appender.setAppend(false);
        appender.setPrudent(true);
        appender.start();

        assertTrue(appender.isAppend());

        final StatusManager sm = context.getStatusManager();
        // StatusPrinter.print(context);
        final StatusChecker statusChecker = new StatusChecker(context);
        assertEquals(Status.WARN, statusChecker.getHighestLevel(0));
        final List<Status> statusList = sm.getCopyOfStatusList();
        assertTrue("Expecting status list size to be 2 or larger, but was " + statusList.size(), statusList.size() >= 2);
        final String msg1 = statusList.get(1).getMessage();

        assertTrue("Got message [" + msg1 + "]", msg1.startsWith("Setting \"Append\" property"));

        appender.doAppend(new Object());
        appender.stop();
        assertTrue(file.exists());
        assertTrue("failed to delete " + file.getAbsolutePath(), file.delete());
    }

    @Test
    public void fileNameCollision() {
        final String fileName = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "fileNameCollision";

        final FileAppender<Object> appender0 = new FileAppender<>();
        appender0.setName("FA0");
        appender0.setFile(fileName);
        appender0.setContext(context);
        appender0.setEncoder(new DummyEncoder<>());
        appender0.start();
        assertTrue(appender0.isStarted());

        final FileAppender<Object> appender1 = new FileAppender<>();
        appender1.setName("FA1");
        appender1.setFile(fileName);
        appender1.setContext(context);
        appender1.setEncoder(new DummyEncoder<>());
        appender1.start();

        assertFalse(appender1.isStarted());

        StatusPrinter.print(context);
        final StatusChecker checker = new StatusChecker(context);
        checker.assertContainsMatch(Status.ERROR, "'File' option has the same value");

    }
}
