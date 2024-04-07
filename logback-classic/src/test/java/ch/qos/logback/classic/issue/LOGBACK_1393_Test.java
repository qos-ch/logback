/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.issue.lbcore258;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.tyler.TylerConfiguratorBase;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.StatusPrinter2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.spi.MDCAdapter;

public class LOGBACK_1393_Test extends TylerConfiguratorBase {

    LoggerContext loggerContext = new LoggerContext();
    LogbackMDCAdapter mdcAdapter = new LogbackMDCAdapter();
    StatusPrinter2 statusPrinter2 = new StatusPrinter2();


    @BeforeEach
    public void setup() {
        loggerContext.setMDCAdapter(mdcAdapter);
    }
    public void configure(LoggerContext loggerCoontext) {
        setContext(loggerCoontext);
        addOnConsoleStatusListener();
        Appender appenderFILE = setupAppenderFILE();
        Logger logger_ROOT = setupLogger("ROOT", "DEBUG", null);
        logger_ROOT.addAppender(appenderFILE);
    }

    Appender setupAppenderFILE() {
        RollingFileAppender appenderFILE = new RollingFileAppender();
        appenderFILE.setContext(loggerContext);
        appenderFILE.setName("FILE");

        appenderFILE.setImmediateFlush(true);
        // Configure component of type TimeBasedRollingPolicy
        TimeBasedRollingPolicy timeBasedRollingPolicy = new TimeBasedRollingPolicy();
        timeBasedRollingPolicy.setContext(loggerContext);
        timeBasedRollingPolicy.setFileNamePattern(subst("/tmp/log/lb1393.%d{yyyy-MM-dd}.log"));
        timeBasedRollingPolicy.setMaxHistory(6);
        timeBasedRollingPolicy.setParent(appenderFILE);
        timeBasedRollingPolicy.start();
        // Inject component of type TimeBasedRollingPolicy into parent
        appenderFILE.setRollingPolicy(timeBasedRollingPolicy);

        // Configure component of type PatternLayoutEncoder
        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setContext(context);
        patternLayoutEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n");

        patternLayoutEncoder.setParent(appenderFILE);
        patternLayoutEncoder.start();
        // Inject component of type PatternLayoutEncoder into parent
        appenderFILE.setEncoder(patternLayoutEncoder);

        // Configure component of type SizeBasedTriggeringPolicy
        SizeBasedTriggeringPolicy sizeBasedTriggeringPolicy = new SizeBasedTriggeringPolicy();
        sizeBasedTriggeringPolicy.setContext(loggerContext);
        sizeBasedTriggeringPolicy.setMaxFileSize(ch.qos.logback.core.util.FileSize.valueOf("1000"));
        // ===========no parent setter
        sizeBasedTriggeringPolicy.start();
        // Inject component of type SizeBasedTriggeringPolicy into parent
        appenderFILE.setTriggeringPolicy(sizeBasedTriggeringPolicy);

        // Configure component of type ThresholdFilter
        ThresholdFilter thresholdFilter = new ThresholdFilter();
        thresholdFilter.setContext(loggerContext);
        thresholdFilter.setLevel("TRACE");
        // ===========no parent setter
        thresholdFilter.start();
        // Inject component of type ThresholdFilter into parent
        appenderFILE.addFilter(thresholdFilter);

        appenderFILE.start();
        return appenderFILE;
    }

    @Test
    void smoke() {
        configure(loggerContext);
        Logger logger = loggerContext.getLogger(this.getClass());
        for(int i = 0; i < 100; i++) {
            logger.atInfo().addKeyValue("i", i).log("hello world xxasdaasfasf asdfasfdsfd");
            delay(100);
        }
        //statusPrinter2.print(loggerContext);
    }

    private void delay(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}


