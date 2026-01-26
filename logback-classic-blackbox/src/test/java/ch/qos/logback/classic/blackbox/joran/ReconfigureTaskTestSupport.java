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

package ch.qos.logback.classic.blackbox.joran;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.blackbox.joran.spi.ConfigFileServlet;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.HttpUtil;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.testUtil.RandomUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.concurrent.CountDownLatch;

public class ReconfigureTaskTestSupport {

    protected int diff = RandomUtil.getPositiveInt();
    protected LoggerContext loggerContext = new LoggerContext();

    protected void configure(InputStream is) throws JoranException {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        jc.doConfigure(is);
    }

    protected CountDownLatch registerChangeDetectedListener() {
        CountDownLatch latch = new CountDownLatch(1);
        ChangeDetectedListener changeDetectedListener = new ChangeDetectedListener(latch);
        loggerContext.addConfigurationEventListener(changeDetectedListener);
        return latch;
    }

    protected static ByteArrayInputStream asBAIS(String configurationStr) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(configurationStr.getBytes("UTF-8"));
    }

    protected String get(String urlString) throws MalformedURLException {
        HttpUtil httpGetUtil = new HttpUtil(HttpUtil.RequestMethod.GET, urlString);
        HttpURLConnection getConnection = httpGetUtil.connectTextTxt();
        String response = httpGetUtil.readResponse(getConnection);
        return response;
    }

    protected String post(String urlString, String val) throws MalformedURLException {
        HttpUtil httpPostUtil1 = new HttpUtil(HttpUtil.RequestMethod.POST, urlString);
        HttpURLConnection postConnection1 = httpPostUtil1.connectTextTxt();
        httpPostUtil1.post(postConnection1, ConfigFileServlet.CONTENT_KEY+ CoreConstants.EQUALS_CHAR+val);
        String response = httpPostUtil1.readResponse(postConnection1);
        return response;
    }

    protected CountDownLatch registerPartialConfigurationEndedSuccessfullyEventListener() {
        CountDownLatch latch = new CountDownLatch(1);
        PartialConfigurationEndedSuccessfullyEventListener listener = new PartialConfigurationEndedSuccessfullyEventListener(latch);
        loggerContext.addConfigurationEventListener(listener);
        return latch;
    }
}
