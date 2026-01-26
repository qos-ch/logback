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

package ch.qos.logback.classic.blackbox.joran.spi;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.blackbox.joran.ReconfigureOnChangeTaskTest;
import ch.qos.logback.classic.blackbox.joran.ReconfigureTaskTestSupport;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.joran.spi.HttpUtil;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.testUtil.RandomUtil;
import jakarta.servlet.http.HttpServlet;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationWatchListTest extends ReconfigureTaskTestSupport {

    static String BAZINGA_LOGGER_NAME = "com.bazinga";
    static String BAZINGA_LOGGER_SETUP_0 = "logback.logger."+BAZINGA_LOGGER_NAME+"=WARN";
    static String BAZINGA_LOGGER_SETUP_1 = "logback.logger."+BAZINGA_LOGGER_NAME+"=ERROR";

    int randomPort = RandomUtil.getRandomServerPort();
    ConfigEmbeddedJetty configEmbeddedJetty;
    ConfigurationWatchList cwl = new ConfigurationWatchList();
    static String FOO_PROPERTIES = "/foo.properties";
    String urlString = "http://127.0.0.1:"+randomPort+FOO_PROPERTIES;

    @BeforeEach
    public void setUp() throws Exception {
        Logger rootLogger = (Logger) LoggerFactory.getLogger( Logger.ROOT_LOGGER_NAME );
        rootLogger.setLevel(Level.INFO);

        configEmbeddedJetty = new ConfigEmbeddedJetty(randomPort);

        cwl.setContext(loggerContext);

        HttpServlet configServlet = new ConfigFileServlet(BAZINGA_LOGGER_SETUP_0);
        configEmbeddedJetty.getServletMap().put(FOO_PROPERTIES, configServlet);

        configEmbeddedJetty.init();

    }

    @AfterEach
    public void tearDown() throws Exception {
        configEmbeddedJetty.stop();
    }



    @Test
    public void testInfrastructure() throws MalformedURLException {
        String response = get(urlString);
        assertNotNull(response);
        Assertions.assertEquals(BAZINGA_LOGGER_SETUP_0, response);

        String setResponse1 = "bla bla";
        String response1 = post(urlString, setResponse1);
        assertEquals(response1, setResponse1);

        String response2 = get(urlString);
        assertEquals(response1, response2);
    }

    @Test
    public void smoke() throws MalformedURLException {
        URL url = new URL(urlString);
        cwl.addToWatchList(url);
        URL changedURL0 = cwl.changeDetectedInURL();
        assertNull(changedURL0);
      
        String setResponse1 = "bla bla";
        String response1 = post(urlString, setResponse1);
        assertEquals(response1, setResponse1);

        URL changedURL1 = cwl.changeDetectedInURL();
        assertEquals(urlString, changedURL1.toString());

        URL changedURL2 = cwl.changeDetectedInURL();
        assertNull(changedURL2);

        URL changedURL3 = cwl.changeDetectedInURL();
        assertNull(changedURL3);
    }

    @Disabled
    @Test
    public void propertiesFromHTTP() throws UnsupportedEncodingException, JoranException, InterruptedException, MalformedURLException {



        String propertiesURLStr = urlString;
        Logger bazingaLogger = loggerContext.getLogger(BAZINGA_LOGGER_NAME);

        assertEquals(BAZINGA_LOGGER_SETUP_0, get(urlString));

        String configurationStr = "<configuration debug=\"true\" scan=\"true\" scanPeriod=\"1 millisecond\"><propertiesConfigurator url=\"" + propertiesURLStr + "\"/></configuration>";

        configure(asBAIS(configurationStr));

        // allow for the first update
        Thread.sleep(50);
        assertEquals(Level.WARN, bazingaLogger.getLevel());
        System.out.println("first test passed with success");

        CountDownLatch changeDetectedLatch0 = registerChangeDetectedListener();
        CountDownLatch configurationDoneLatch0 = registerPartialConfigurationEndedSuccessfullyEventListener();

        String response1 = post(urlString, BAZINGA_LOGGER_SETUP_1);
        assertEquals(BAZINGA_LOGGER_SETUP_1, get(urlString));

        changeDetectedLatch0.await(100, TimeUnit.MICROSECONDS);
        configurationDoneLatch0.await(100, TimeUnit.MICROSECONDS);
        assertEquals(Level.ERROR, bazingaLogger.getLevel());
    }

}
