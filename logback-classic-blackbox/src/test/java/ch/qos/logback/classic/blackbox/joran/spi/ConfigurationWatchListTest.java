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

package ch.qos.logback.classic.blackbox.joran.spi;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
//import ch.qos.logback.classic.blackbox.joran.ReconfigureOnChangeTaskTest;
//import ch.qos.logback.classic.blackbox.joran.ReconfigureOnChangeTaskTest;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.joran.spi.HttpUtil;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.testUtil.RandomUtil;
import jakarta.servlet.http.HttpServlet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationWatchListTest {


//    static String BAZINGA_LOGGER_0 = "logback.logger.com.bazinga=WARN";
//    static String BAZINGA_LOGGER_1 = "logback.logger.com.bazinga=ERROR"
//
//    int randomPort = RandomUtil.getRandomServerPort();
//    ConfigEmbeddedJetty configEmbeddedJetty;
//    LoggerContext loggerContext = new LoggerContext();
//    ConfigurationWatchList cwl = new ConfigurationWatchList();
//    String urlString = "http://127.0.0.1:"+randomPort+"/";
//
//    @BeforeEach
//    public void setUp() throws Exception {
//        Logger rootLogger = (Logger) LoggerFactory.getLogger( Logger.ROOT_LOGGER_NAME );
//        rootLogger.setLevel(Level.INFO);
//
//        configEmbeddedJetty = new ConfigEmbeddedJetty(randomPort);
//
//        cwl.setContext(loggerContext);
//
//        HttpServlet configServlet = new ConfigFileServlet();
//        configEmbeddedJetty.getServletMap().put("/", configServlet);
//        //configEmbeddedJetty.getServletMap().put("/mod", configServlet);
//
//        configEmbeddedJetty.init();
//
//    }
//
//    @AfterEach
//    public void tearDown() throws Exception {
//        configEmbeddedJetty.stop();
//    }
//
//    @Test
//    public void testInfrastructure() throws MalformedURLException {
//        HttpUtil httpGetUtil0 = new HttpUtil(HttpUtil.RequestMethod.GET, urlString);
//
//        HttpURLConnection getConnection0 = httpGetUtil0.connectTextTxt();
//        String response = httpGetUtil0.readResponse(getConnection0);
//        assertNotNull(response);
//        Assertions.assertEquals(ConfigFileServlet.DEFAULT_CONTENT, response);
//
//        HttpUtil httpPostUtil1 = new HttpUtil(HttpUtil.RequestMethod.POST, urlString);
//        HttpURLConnection postConnection1 = httpPostUtil1.connectTextTxt();
//        String setResponse1 = "bla bla";
//        httpPostUtil1.post(postConnection1, ConfigFileServlet.CONTENT_KEY+ CoreConstants.EQUALS_CHAR+setResponse1);
//
//        String response1 = httpPostUtil1.readResponse(postConnection1);
//        assertEquals(response1, setResponse1);
//        //System.out.println( "POST response1="+response1);
//        HttpUtil httpGetUtil2 = new HttpUtil(HttpUtil.RequestMethod.GET, urlString);
//
//        HttpURLConnection getConnection2 = httpGetUtil2.connectTextTxt();
//        String response2 = httpGetUtil2.readResponse(getConnection2);
//        assertEquals(response1, response2);
//
//    }
//
//    @Test
//    public void smoke() throws MalformedURLException {
//        URL url = new URL(urlString);
//        cwl.addToWatchList(url);
//        URL changedURL0 = cwl.changeDetectedInURL();
//        assertNull(changedURL0);
//        HttpUtil httpPostUtil1 = new HttpUtil(HttpUtil.RequestMethod.POST, urlString);
//        HttpURLConnection postConnection1 = httpPostUtil1.connectTextTxt();
//        String setResponse1 = "bla bla";
//        httpPostUtil1.post(postConnection1, ConfigFileServlet.CONTENT_KEY+ CoreConstants.EQUALS_CHAR+setResponse1);
//
//        String response1 = httpPostUtil1.readResponse(postConnection1);
//        assertEquals(response1, setResponse1);
//        URL changedURL1 = cwl.changeDetectedInURL();
//        assertEquals(urlString, changedURL1.toString());
//
//        URL changedURL2 = cwl.changeDetectedInURL();
//        assertNull(changedURL2);
//
//        URL changedURL3 = cwl.changeDetectedInURL();
//        assertNull(changedURL3);
//    }
//
//
//    @Test
//    public void propertiesFromHTTP() throws UnsupportedEncodingException, JoranException {
//        String loggerName = "com.bazinga";
//        String propertiesURLStr = "https://127.0.0.1:"+randomPort+"/";
//        Logger aLogger = loggerContext.getLogger(loggerName);
//        String configurationStr = "<configuration debug=\"true\" scan=\"true\" scanPeriod=\"10 millisecond\"><propertiesConfigurator url=\"" + propertiesURLStr + "\"/></configuration>";
//
//        configure(asBAIS(configurationStr));
//
//        assertEquals(Level.WARN, aLogger.getLevel());
//        System.out.println("first phase OK");
//        CountDownLatch changeDetectedLatch0 = registerChangeDetectedListener();
//        CountDownLatch configurationDoneLatch0 = registerPartialConfigurationEndedSuccessfullyEventListener();
//
//        changeDetectedLatch0.await();
//        System.out.println("after changeDetectedLatch0.await();");
//        configurationDoneLatch0.await();
//        assertEquals(Level.ERROR, aLogger.getLevel());
//    }
//

}
