/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.net.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.helpers.BasicMarker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ClassPackagingData;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.LoggerRemoteView;
import ch.qos.logback.classic.spi.LoggingEventVO;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyVO;
import ch.qos.logback.core.net.HardenedObjectInputStream;

public class HardenedLoggingEventInputStream extends HardenedObjectInputStream {

    static final String ARRAY_PREFIX = "[L";
    
    static public List<String> getWhilelist() {
        List<String> whitelist = new ArrayList<String>();
        whitelist.add(LoggingEventVO.class.getName());
        whitelist.add(LoggerContextVO.class.getName());
        whitelist.add(LoggerRemoteView.class.getName());
        whitelist.add(ThrowableProxyVO.class.getName());
        whitelist.add(BasicMarker.class.getName());
        whitelist.add(Level.class.getName());
        whitelist.add(Logger.class.getName());
        whitelist.add(StackTraceElement.class.getName());
        whitelist.add(StackTraceElement[].class.getName());
        whitelist.add(ThrowableProxy.class.getName());
        whitelist.add(ThrowableProxy[].class.getName());
        whitelist.add(IThrowableProxy.class.getName());
        whitelist.add(IThrowableProxy[].class.getName());
        whitelist.add(StackTraceElementProxy.class.getName());
        whitelist.add(StackTraceElementProxy[].class.getName());
        whitelist.add(ClassPackagingData.class.getName());

        return whitelist;
    }
   
    public HardenedLoggingEventInputStream(InputStream is) throws IOException {
        super(is, getWhilelist());
    }
    
    public HardenedLoggingEventInputStream(InputStream is, List<String> additionalAuthorizedClasses) throws IOException {
        this(is);
        super.addToWhitelist(additionalAuthorizedClasses);
    }
}
