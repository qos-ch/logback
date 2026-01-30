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

package ch.qos.logback.classic.issue.github1010;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.net.SimpleSocketServer;
import ch.qos.logback.classic.net.SocketAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.LoggingEventVO;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusListenerConfigHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SocketAppender1010Test {


    private static final String HOST = "localhost";
    private final int port = RandomUtil.getRandomServerPort();
    private final String mdcKey = "moo" + RandomUtil.getPositiveInt();
    private final String mdcVal = "mdcVal" + RandomUtil.getPositiveInt();
    static final String LIST_APPENDER = "LIST_APPENDER";


    LogbackMDCAdapter mdcAdapterForClient = new LogbackMDCAdapter();

    @Test
    @Timeout(value = 500, unit = MILLISECONDS)
    public void smoke() {
        System.out.println("Running on port " + port);
        LoggerContext serverLoggerContext = buildAndConfigureContextForServer();
        SimpleSocketServer simpleSocketServer = new SimpleSocketServer(serverLoggerContext, port);
        simpleSocketServer.start();

        // wait until server is up
        yieldLoop(Thread::isAlive, simpleSocketServer);

        LoggerContext clientLoggerContext = buildAndConfigureContextForClient(mdcAdapterForClient);
        Logger clientLogger = clientLoggerContext.getLogger(SocketAppender1010Test.class);
        Logger serverRoot = serverLoggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        mdcAdapterForClient.put(mdcKey, mdcVal);
        clientLogger.info("hello");

        StatusManager clientStatusManager = clientLoggerContext.getStatusManager();

        // wait until connection is established
        yieldLoop(csm -> csm.getCopyOfStatusList().stream().anyMatch(s -> s.getMessage().contains("connection established")), clientStatusManager);

        ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) serverRoot.getAppender(LIST_APPENDER);
        assertNotNull(listAppender);
        assertNotNull(listAppender.list);
        yieldLoop(( list -> !list.isEmpty()), listAppender.list);


        assertEquals(1, listAppender.list.size());
        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertNotNull(loggingEvent);
        assertTrue(loggingEvent instanceof LoggingEventVO);
        LoggingEventVO loggingEventVO = (LoggingEventVO) loggingEvent;
        Map<String, String> mdcMap = loggingEventVO.getMdc();
        assertNotNull(mdcMap);
        assertEquals(mdcVal, mdcMap.get(mdcKey));

    }


    <T> void yieldLoop(Predicate<T> predicate, T arg) {
        while(true) {
            if(predicate.test(arg)) {
                break;
            }
            Thread.yield();
        }
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    LoggerContext buildAndConfigureContextForServer() {
        LoggerContext context = new LoggerContext();
        context.setName("serverContext");
        LogbackMDCAdapter mdcAdapter = new LogbackMDCAdapter();
        context.setMDCAdapter(mdcAdapter);

        StatusListenerConfigHelper.addOnConsoleListenerInstance(context, new OnConsoleStatusListener());

        Logger root = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);


        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.setName(LIST_APPENDER);
        listAppender.setContext(context);
        listAppender.start();
        root.addAppender(listAppender);

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setName("STDOUT");

//        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
//        patternLayoutEncoder.setContext(context);
//        patternLayoutEncoder.setPattern("%-4relative [%thread] %level %logger{35} =%mdc= -%kvp- %msg %n");
//        patternLayoutEncoder.setParent(consoleAppender);
//        patternLayoutEncoder.start();
//        consoleAppender.setEncoder(patternLayoutEncoder);
//        consoleAppender.start();
//        root.addAppender(consoleAppender);

        return context;
    }


    LoggerContext buildAndConfigureContextForClient(LogbackMDCAdapter mdcAdapter) {
        LoggerContext context = new LoggerContext();
        context.setName("clientContext");

        context.setMDCAdapter(mdcAdapter);

        StatusListenerConfigHelper.addOnConsoleListenerInstance(context, new OnConsoleStatusListener());

        SocketAppender socketAppender = buildSocketAppender(context);

        Logger root = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);
        root.addAppender(socketAppender);

        return context;
    }


    SocketAppender buildSocketAppender(LoggerContext context) {
        SocketAppender socketAppender = new SocketAppender();
        socketAppender.setContext(context);
        socketAppender.setName("socketAppender");
        socketAppender.setRemoteHost(HOST);
        socketAppender.setPort(port);
        socketAppender.setReconnectionDelay(ch.qos.logback.core.util.Duration.valueOf("100"));
        socketAppender.setIncludeCallerData(true);

        socketAppender.start();
        return socketAppender;
    }

}
