/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses( { LoggerContextTest.class, LoggerPerfTest.class,
    ScenarioBasedLoggerContextTest.class, PatternLayoutTest.class,
    LoggerTest.class, LoggerSerializationTest.class,
    MessageFormattingTest.class, MDCTest.class,
    TurboFilteringInLoggerTest.class })

    
public class PackageTest {
}