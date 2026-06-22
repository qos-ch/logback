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

package ch.qos.logback.classic.issue.logback1159;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.Set;

//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang3.RandomStringUtils;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.testUtil.FileTestUtil;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.FileUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class LogbackListenerTest {
    static String LLT_TARGET_DIR_KEY = "lltTargetDir";
    static String LLT_TARGET_DIR_PREFIX = "target/llt";

    int diff = RandomUtil.getPositiveInt();
    private String lltTargetDirValue = LLT_TARGET_DIR_PREFIX+diff;
    private File logFile = new File(lltTargetDirValue +"/test.log");

    LoggerContext loggerContext = new LoggerContext();
    LogbackMDCAdapter mdcAdapter = new LogbackMDCAdapter();

    @BeforeEach
    void setUp() {
        loggerContext.putProperty(LLT_TARGET_DIR_KEY, lltTargetDirValue);
        loggerContext.setMDCAdapter(mdcAdapter);

    }

    private void doConfigure() throws JoranException {
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        configurator.doConfigure(new File("src/test/input/issue/logback-1159.xml"));
    }

    @AfterEach
    public void after() {
        logFile.delete();
    }

    private void disableLogFileAccess() throws IOException {
        FileUtil.createMissingParentDirectories(logFile);
        logFile.createNewFile();
        logFile.deleteOnExit();
        Path path = Paths.get(logFile.toURI());
        Set<PosixFilePermission> permissions = Collections.emptySet();
        try {
            Files.setPosixFilePermissions(path, permissions);
        } catch (UnsupportedOperationException e) {
            path.toFile().setReadOnly();
        }
    }

    @Test
    public void testThatErrorIsDetectedAtLogInit() throws Exception {
        Assertions.assertThrows(LoggingError.class, () -> {
            disableLogFileAccess();
            doConfigure();
        });
    }

    @Test
    public void assertThatNonFailSafeAppendersNotAffected() throws JoranException {
        doConfigure();
        Logger logger = LoggerFactory.getLogger("NOTJOURNAL");
        logger.error("This should not fail");
    }

}