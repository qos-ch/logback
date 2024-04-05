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
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.tyler.TylerConfiguratorBase;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class LOGBACK_1393_Test extends TylerConfiguratorBase {

    LoggerContext loggerCoontext = new LoggerContext();

    public void configure(LoggerContext loggerCoontext) {
        setContext(loggerCoontext);
        propertyModelHandlerHelper.handlePropertyModel(this, "LOG_HOME", "log", "", "", "");
        Appender appenderFILE = setupAppenderFILE();
        Logger logger_com_mindsphere_china_poc_connectivity = setupLogger("com.mindsphere.china.poc.connectivity",
                "DEBUG", Boolean.FALSE);
        logger_com_mindsphere_china_poc_connectivity.addAppender(appenderFILE);
        Logger logger_ROOT = setupLogger("ROOT", "ERROR", null);
        logger_ROOT.addAppender(appenderFILE);
    }

    Appender setupAppenderFILE() {
        RollingFileAppender appenderFILE = new RollingFileAppender();
        appenderFILE.setContext(loggerCoontext);
        appenderFILE.setName("FILE");

        // Configure component of type TimeBasedRollingPolicy
        TimeBasedRollingPolicy timeBasedRollingPolicy = new TimeBasedRollingPolicy();
        timeBasedRollingPolicy.setContext(loggerCoontext);
        timeBasedRollingPolicy.setFileNamePattern(subst("${LOG_HOME}/connectivi.log.%d{yyyy-MM-dd}.log"));
        timeBasedRollingPolicy.setMaxHistory(6);
        timeBasedRollingPolicy.setParent(appenderFILE);
        timeBasedRollingPolicy.start();
        // Inject component of type TimeBasedRollingPolicy into parent
        appenderFILE.setRollingPolicy(timeBasedRollingPolicy);

        // Configure component of type PatternLayoutEncoder
        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setContext(context);
        patternLayoutEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n");
        patternLayoutEncoder.setImmediateFlush(true);
        patternLayoutEncoder.setParent(appenderFILE);
        patternLayoutEncoder.start();
        // Inject component of type PatternLayoutEncoder into parent
        appenderFILE.setEncoder(patternLayoutEncoder);

        // Configure component of type SizeBasedTriggeringPolicy
        SizeBasedTriggeringPolicy sizeBasedTriggeringPolicy = new SizeBasedTriggeringPolicy();
        sizeBasedTriggeringPolicy.setContext(loggerCoontext);
        sizeBasedTriggeringPolicy.setMaxFileSize(ch.qos.logback.core.util.FileSize.valueOf("10MB"));
        // ===========no parent setter
        sizeBasedTriggeringPolicy.start();
        // Inject component of type SizeBasedTriggeringPolicy into parent
        appenderFILE.setTriggeringPolicy(sizeBasedTriggeringPolicy);

        // Configure component of type ThresholdFilter
        ThresholdFilter thresholdFilter = new ThresholdFilter();
        thresholdFilter.setContext(loggerCoontext);
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

        FileNamePattern fnp = new FileNamePattern("/log/connectivi.log.%d{yyyy-MM-dd}.log", context);
        Instant now = Instant.now();
        fnp.toRegexForFixedDate(now);
    }

}


