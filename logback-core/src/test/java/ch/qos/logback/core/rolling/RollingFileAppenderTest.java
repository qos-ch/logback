/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.rolling;

import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.FileUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.appender.AbstractAppenderTest;
import ch.qos.logback.core.testUtil.DummyEncoder;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.status.testUtil.StatusChecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
//import ch.qos.logback.core.util.StatusPrinter;

public class RollingFileAppenderTest extends AbstractAppenderTest<Object> {

    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    Context context = new ContextBase();

    TimeBasedRollingPolicy<Object> tbrp = new TimeBasedRollingPolicy<Object>();
    int diff = RandomUtil.getPositiveInt();
    String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/";
    DummyEncoder<Object> encoder;

    @BeforeEach
    public void setUp() throws Exception {
        // noStartTest fails if the context is set in setUp
        // rfa.setContext(context);
        encoder = new DummyEncoder<>();
        rfa.setEncoder(encoder);
        rfa.setName("test");
        tbrp.setContext(context);
        tbrp.setParent(rfa);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Override
    protected Appender<Object> getAppender() {
        return rfa;
    }

    @Override
    protected Appender<Object> getConfiguredAppender() {
        rfa.setContext(context);
        tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%d.log");
        tbrp.start();
        rfa.setRollingPolicy(tbrp);

        rfa.start();
        return rfa;
    }

    @Test
    public void testPrudentModeLogicalImplications() {
        rfa.setContext(context);
        // prudent mode will force "file" property to be null
        rfa.setFile("some non null value");
        rfa.setAppend(false);
        rfa.setPrudent(true);

        tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%d.log");
        tbrp.start();
        rfa.setRollingPolicy(tbrp);

        rfa.start();

        Assertions.assertTrue(rfa.isAppend());
        Assertions.assertNull(rfa.rawFileProperty());
        Assertions.assertTrue(rfa.isStarted());
    }

    @Test
    public void testPrudentModeLogicalImplicationsOnCompression() {
        rfa.setContext(context);
        rfa.setAppend(false);
        rfa.setPrudent(true);

        tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%d.log.zip");
        tbrp.start();
        rfa.setRollingPolicy(tbrp);

        rfa.start();

        StatusChecker checker = new StatusChecker(context);
        Assertions.assertFalse(rfa.isStarted());
        Assertions.assertEquals(Status.ERROR, checker.getHighestLevel(0));
    }

    @Test
    public void testFilePropertyAfterRollingPolicy() {
        rfa.setContext(context);
        rfa.setRollingPolicy(tbrp);
        rfa.setFile("x");
        // StatusPrinter.print(context);
        StatusChecker statusChecker = new StatusChecker(context.getStatusManager());
        statusChecker.assertContainsMatch(Status.ERROR, "File property must be set before any triggeringPolicy ");
    }

    @Test
    public void testFilePropertyAfterTriggeringPolicy() {
        rfa.setContext(context);
        rfa.setTriggeringPolicy(new SizeBasedTriggeringPolicy<Object>());
        rfa.setFile("x");
        StatusChecker statusChecker = new StatusChecker(context.getStatusManager());
        statusChecker.assertContainsMatch(Status.ERROR, "File property must be set before any triggeringPolicy ");
    }

    @Test
    public void testFileNameWithParenthesis() {
        // if ')' is not escaped, the test throws
        // java.lang.IllegalStateException: FileNamePattern
        // [.../program(x86)/toto-%d.log] does not contain a valid
        // DateToken
        rfa.setContext(context);
        tbrp.setFileNamePattern(randomOutputDir + "program(x86)/toto-%d.log");
        tbrp.start();
        rfa.setRollingPolicy(tbrp);
        rfa.start();
        rfa.doAppend("hello");
    }

    @Test
    public void stopTimeBasedRollingPolicy() {
        rfa.setContext(context);

        tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%d.log.zip");
        tbrp.start();
        rfa.setRollingPolicy(tbrp);
        rfa.start();

        //StatusPrinter.print(context);
        Assertions.assertTrue(tbrp.isStarted());
        Assertions.assertTrue(rfa.isStarted());
        rfa.stop();
        Assertions.assertFalse(rfa.isStarted());
        Assertions.assertFalse(tbrp.isStarted());

    }

    @Test
    public void stopFixedWindowRollingPolicy() {
        rfa.setContext(context);
        rfa.setFile(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-.log");

        FixedWindowRollingPolicy fwRollingPolicy = new FixedWindowRollingPolicy();
        fwRollingPolicy.setContext(context);
        fwRollingPolicy.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%i.log.zip");
        fwRollingPolicy.setParent(rfa);
        fwRollingPolicy.start();
        SizeBasedTriggeringPolicy<Object> sbTriggeringPolicy = new SizeBasedTriggeringPolicy<Object>();
        sbTriggeringPolicy.setContext(context);
        sbTriggeringPolicy.start();

        rfa.setRollingPolicy(fwRollingPolicy);
        rfa.setTriggeringPolicy(sbTriggeringPolicy);

        rfa.start();

        // StatusPrinter.print(context);
        Assertions.assertTrue(fwRollingPolicy.isStarted());
        Assertions.assertTrue(sbTriggeringPolicy.isStarted());
        Assertions.assertTrue(rfa.isStarted());
        rfa.stop();
        Assertions.assertFalse(rfa.isStarted());
        Assertions.assertFalse(fwRollingPolicy.isStarted());
        Assertions.assertFalse(sbTriggeringPolicy.isStarted());

    }

    /**
     * Test for http://jira.qos.ch/browse/LOGBACK-796
     */
    @Test
    public void testFileShouldNotMatchFileNamePattern() {
        rfa.setContext(context);
        rfa.setFile(CoreTestConstants.OUTPUT_DIR_PREFIX + "x-2013-04.log");
        tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "x-%d{yyyy-MM}.log");
        tbrp.start();

        rfa.setRollingPolicy(tbrp);
        rfa.start();
        StatusChecker statusChecker = new StatusChecker(context);
        final String msg = "File property collides with fileNamePattern. Aborting.";
        boolean containsMatch = statusChecker.containsMatch(Status.ERROR, msg);
        Assertions.assertTrue(containsMatch, "Missing error: " + msg);
    }

    @Test
    public void collidingTimeformat() {
        rfa.setContext(context);
        rfa.setAppend(false);
        rfa.setPrudent(true);

        tbrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "toto-%d{dd}.log.zip");
        tbrp.start();
        rfa.setRollingPolicy(tbrp);

        rfa.start();

        StatusChecker checker = new StatusChecker(context);
        Assertions.assertFalse(rfa.isStarted());
        Assertions.assertEquals(Status.ERROR, checker.getHighestLevel(0));
        // StatusPrinter.print(context);
        checker.assertContainsMatch("The date format in FileNamePattern will result");
    }

    @Test
    @DisplayName("Checks header and footer are written when the files are rolled")
    public void testHeaderFooterWritten() throws IOException, InterruptedException {

        String folderPrefix = CoreTestConstants.OUTPUT_DIR_PREFIX+diff+"/";
        String namePrefix = folderPrefix+"header-";
        File folderFile = new File(folderPrefix);
        FileUtil.createMissingParentDirectories(folderFile);


        encoder.setFileHeader("HEADER");
        encoder.setFileFooter("FOOTER");
        rfa.setContext(context);
        FixedWindowRollingPolicy fixedWindowRollingPolicy = new FixedWindowRollingPolicy();
        fixedWindowRollingPolicy.setContext(context);
        fixedWindowRollingPolicy.setParent(rfa);
        fixedWindowRollingPolicy.setMaxIndex(3);
        String fileNamePattern = namePrefix + "%i.log";
        fixedWindowRollingPolicy.setFileNamePattern(fileNamePattern);
        rfa.setRollingPolicy(fixedWindowRollingPolicy);
        rfa.setFile(namePrefix+"0.log");
        fixedWindowRollingPolicy.start();
        rfa.setImmediateFlush(true);
        SizeBasedTriggeringPolicy<Object> sbtp = new SizeBasedTriggeringPolicy<>();
        sbtp.setMaxFileSize(new FileSize(10));
        sbtp.setCheckIncrement(Duration.buildByMilliseconds(10));

        rfa.setTriggeringPolicy(sbtp);
        rfa.getTriggeringPolicy().start();
        rfa.start();

        for (int i = 0; i < 100; i++) {
            rfa.doAppend("data" + i);
            File file = new File(namePrefix + fixedWindowRollingPolicy.getMaxIndex() + ".log");
            if (file.exists()) {
                break;
            }
            Thread.sleep(5);
        }
        rfa.stop();

        for (int i = 0; i < fixedWindowRollingPolicy.getMaxIndex(); i++) {
            File file = new File(namePrefix + i + ".log");
            Assertions.assertTrue(file.exists());
            List<String> lines = Files.readAllLines(file.toPath());
            Assertions.assertTrue(lines.size() > 2, "At least 2 lines per file are expected in " + file);
            Assertions.assertEquals("HEADER", lines.get(0));
            Assertions.assertEquals("FOOTER", lines.get(lines.size() - 1));
            Assertions.assertEquals(1, Collections.frequency(lines, "HEADER"));
            Assertions.assertEquals(1, Collections.frequency(lines, "FOOTER"));
        }
    }

}
