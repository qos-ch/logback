/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 *  Copyright (C) 1999-2025, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *     or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.classic.joran;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusPrinter2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.spi.MDCAdapter;

import static ch.qos.logback.core.model.processor.FileCollisionAnalyser.COLLISION_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;

public class FileCollisionAnalyserTest {

    LoggerContext loggerContext = new LoggerContext();
    MDCAdapter mdcAdapter = new LogbackMDCAdapter();
    Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    StatusPrinter2 statusPrinter2 = new StatusPrinter2();
    StatusChecker checker = new StatusChecker(loggerContext);
    int diff = RandomUtil.getPositiveInt();

    String aLoggerName = "ch.qos.logback";
    Logger aLogger = loggerContext.getLogger(aLoggerName);

    String outputTargetVal = ClassicTestConstants.OUTPUT_DIR_PREFIX + "collision/output-" + diff + ".log";
    String fileNamePatternVal = ClassicTestConstants.OUTPUT_DIR_PREFIX + "collision/output-%d{yyyy-MM-dd}-" + diff + ".log";

    void configure(String file) throws JoranException {
        loggerContext.setMDCAdapter(mdcAdapter);
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        loggerContext.putProperty("outputTargetKey", outputTargetVal);
        loggerContext.putProperty("fileNamePatternKey", fileNamePatternVal);

        jc.doConfigure(file);

    }

    @Test
    public void fileCollision() throws JoranException {
        String configFile = ClassicTestConstants.JORAN_INPUT_PREFIX + "collision/repeatFile.xml";
        runCollisionTest(configFile, 1, 0, "file", outputTargetVal);
    }


    @Test
    public void testRollingFileAppenderCollisionByFile() throws JoranException {
        String configFile = ClassicTestConstants.JORAN_INPUT_PREFIX + "collision/repeatRollingFileAppenderByFile.xml";
        runCollisionTest(configFile, 0, 1, "file", outputTargetVal);
    }

    @Test
    public void testRollingFileAppenderCollisionByFilePattern() throws JoranException {
        String configFile = ClassicTestConstants.JORAN_INPUT_PREFIX + "collision/repeatRollingFileAppenderByFilePattern.xml";
        runCollisionTest(configFile, 0,1, "fileNamePattern", fileNamePatternVal);
    }

    @Test
    public void testMixedFileaAppenderRollingFileAppenderCollisionByFile() throws JoranException {
        String configFile = ClassicTestConstants.JORAN_INPUT_PREFIX + "collision/repeatMixedFileAndRolling.xml";
        runCollisionTest(configFile, 1, 0, "file", outputTargetVal);
    }

    @Test
    public void testConditionalFileCollision() throws JoranException {
        String configFile = ClassicTestConstants.JORAN_INPUT_PREFIX + "collision/conditionalRepeat.xml";
        configure(configFile);
        //statusPrinter2.print(loggerContext);

        Appender<ILoggingEvent> fileAppender1 = root.getAppender("FILE1");
        assertNull(fileAppender1);

        Appender<ILoggingEvent> fileAppender2 = root.getAppender("FILE2");
        assertNotNull(fileAppender2);
        checker.assertIsWarningOrErrorFree();
    }


    public void runCollisionTest(String configFile, int fileAppenderCount, int rollingAppenderCount, String tagName, String value) throws JoranException {
        configure(configFile);
        //statusPrinter2.print(loggerContext);

        Appender<ILoggingEvent> fileAppender1 = root.getAppender("FILE1");
        assertNotNull(fileAppender1);

        Appender<ILoggingEvent> fileAppender2 = aLogger.getAppender("FILE2");
        assertNull(fileAppender2);

        //statusPrinter2.print(loggerContext);

        String expectationPattern = COLLISION_MESSAGE.replace("[", "\\[").replace("]", "\\]");

        String sanitizeValue = value.replace("{", "\\{").replace("}", "\\}");
        String expected = String.format(expectationPattern, "FILE2", tagName, sanitizeValue, "FILE1");
        checker.assertContainsMatch(Status.ERROR, expected);
        checker.assertMatchCount("About to instantiate appender of type \\[" + FileAppender.class.getName() + "\\]", fileAppenderCount);
        checker.assertMatchCount("About to instantiate appender of type \\[" + RollingFileAppender.class.getName() + "\\]", rollingAppenderCount);
    }


}
