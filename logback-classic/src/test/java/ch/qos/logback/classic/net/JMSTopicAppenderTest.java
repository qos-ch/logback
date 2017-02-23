/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Properties;

import javax.jms.ObjectMessage;
import javax.naming.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.net.mock.MockTopic;
import ch.qos.logback.classic.net.mock.MockTopicConnectionFactory;
import ch.qos.logback.classic.net.mock.MockTopicPublisher;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.MockInitialContext;
import ch.qos.logback.classic.util.MockInitialContextFactory;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.spi.PreSerializationTransformer;

public class JMSTopicAppenderTest {

    ch.qos.logback.core.Context context;
    JMSTopicAppender appender;
    PreSerializationTransformer<ILoggingEvent> pst = new LoggingEventPreSerializationTransformer();

    @Before
    public void setUp() throws Exception {
        context = new ContextBase();
        appender = new JMSTopicAppender();
        appender.setContext(context);
        appender.setName("jmsTopic");
        appender.tcfBindingName = "topicCnxFactory";
        appender.topicBindingName = "testTopic";
        appender.setProviderURL("url");
        appender.setInitialContextFactoryName(MockInitialContextFactory.class.getName());

        MockInitialContext mic = MockInitialContextFactory.getContext();
        mic.map.put(appender.tcfBindingName, new MockTopicConnectionFactory());
        mic.map.put(appender.topicBindingName, new MockTopic(appender.topicBindingName));

    }

    @After
    public void tearDown() throws Exception {
        appender = null;
        context = null;
    }

    @Test
    public void testAppendOk() {
        appender.start();

        ILoggingEvent le = createLoggingEvent();
        appender.append(le);

        MockTopicPublisher tp = (MockTopicPublisher) appender.topicPublisher;
        assertEquals(1, tp.getMessageList().size());
        ObjectMessage message = (ObjectMessage) tp.getMessageList().get(0);
        try {
            Serializable witness = pst.transform(le);
            assertEquals(witness, message.getObject());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testAppendFailure() {
        appender.start();

        // make sure the append method does not work
        appender.topicPublisher = null;

        ILoggingEvent le = createLoggingEvent();
        for (int i = 1; i <= 3; i++) {
            appender.append(le);
            assertEquals(i, context.getStatusManager().getCount());
            assertTrue(appender.isStarted());
        }
        appender.append(le);
        assertEquals(4, context.getStatusManager().getCount());
        assertFalse(appender.isStarted());
    }

    @Test
    public void testBuildEnvProperties() {
        appender.setInitialContextFactoryName("icfn");
        appender.setProviderURL("url");
        appender.setURLPkgPrefixes("pkgPref");
        appender.setSecurityPrincipalName("user");
        appender.setSecurityCredentials("cred");

        Properties props = appender.buildEnvProperties();
        assertEquals(5, props.size());
        assertEquals(appender.getInitialContextFactoryName(), props.getProperty(Context.INITIAL_CONTEXT_FACTORY));
        assertEquals(appender.getProviderURL(), props.getProperty(Context.PROVIDER_URL));
        assertEquals(appender.getURLPkgPrefixes(), props.getProperty(Context.URL_PKG_PREFIXES));
        assertEquals(appender.getSecurityPrincipalName(), props.getProperty(Context.SECURITY_PRINCIPAL));
        assertEquals(appender.getSecurityCredentials(), props.getProperty(Context.SECURITY_CREDENTIALS));
    }

    @Test
    public void testBuildEnvPropertiesWithNullProviderURL() {
        appender.setInitialContextFactoryName("icfn");
        appender.setProviderURL(null);
        appender.setURLPkgPrefixes("pkgPref");
        appender.setSecurityPrincipalName("user");
        appender.setSecurityCredentials("cred");

        Properties props = appender.buildEnvProperties();
        assertEquals(4, props.size());
        assertEquals(appender.getInitialContextFactoryName(), props.getProperty(Context.INITIAL_CONTEXT_FACTORY));
        assertEquals(null, props.getProperty(Context.PROVIDER_URL));
        assertEquals(appender.getURLPkgPrefixes(), props.getProperty(Context.URL_PKG_PREFIXES));
        assertEquals(appender.getSecurityPrincipalName(), props.getProperty(Context.SECURITY_PRINCIPAL));
        assertEquals(appender.getSecurityCredentials(), props.getProperty(Context.SECURITY_CREDENTIALS));

        assertEquals(1, context.getStatusManager().getCount());
    }

    @Test
    public void testBuildEnvPropertiesWithNullCredentials() {
        appender.setInitialContextFactoryName("icfn");
        appender.setProviderURL("url");
        appender.setURLPkgPrefixes("pkgPref");
        appender.setSecurityPrincipalName("user");
        appender.setSecurityCredentials(null);

        Properties props = appender.buildEnvProperties();
        assertEquals(4, props.size());
        assertEquals(appender.getInitialContextFactoryName(), props.getProperty(Context.INITIAL_CONTEXT_FACTORY));
        assertEquals(appender.getProviderURL(), props.getProperty(Context.PROVIDER_URL));
        assertEquals(appender.getURLPkgPrefixes(), props.getProperty(Context.URL_PKG_PREFIXES));
        assertEquals(appender.getSecurityPrincipalName(), props.getProperty(Context.SECURITY_PRINCIPAL));
        assertEquals(null, props.getProperty(Context.SECURITY_CREDENTIALS));

        assertEquals(1, context.getStatusManager().getCount());
    }

    @Test
    public void testBuildEnvPropertiesWithPkgNull() {
        appender.setInitialContextFactoryName("icfn");
        appender.setProviderURL("url");
        appender.setURLPkgPrefixes(null);
        appender.setSecurityPrincipalName("user");
        appender.setSecurityCredentials("cred");

        Properties props = appender.buildEnvProperties();
        assertEquals(4, props.size());
        assertEquals(appender.getInitialContextFactoryName(), props.getProperty(Context.INITIAL_CONTEXT_FACTORY));
        assertEquals(appender.getProviderURL(), props.getProperty(Context.PROVIDER_URL));
        assertEquals(null, props.getProperty(Context.URL_PKG_PREFIXES));
        assertEquals(appender.getSecurityPrincipalName(), props.getProperty(Context.SECURITY_PRINCIPAL));
        assertEquals(appender.getSecurityCredentials(), props.getProperty(Context.SECURITY_CREDENTIALS));

        assertEquals(0, context.getStatusManager().getCount());
    }

    @Test
    public void testStartMinimalInfo() {
        // let's leave only what's in the setup()
        // method, minus the providerURL
        appender.setProviderURL(null);
        appender.start();

        assertTrue(appender.isStarted());

        try {
            assertEquals(appender.topicBindingName, appender.topicPublisher.getTopic().getTopicName());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testStartUserPass() {
        appender.setUserName("test");
        appender.setPassword("test");

        appender.start();

        assertTrue(appender.isStarted());

        try {
            assertEquals(appender.topicBindingName, appender.topicPublisher.getTopic().getTopicName());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testStartFails() {
        appender.topicBindingName = null;

        appender.start();

        assertFalse(appender.isStarted());
    }

    private ILoggingEvent createLoggingEvent() {
        LoggingEvent le = new LoggingEvent();
        le.setLevel(Level.DEBUG);
        le.setMessage("test message");
        le.setTimeStamp(System.currentTimeMillis());
        le.setThreadName(Thread.currentThread().getName());
        return le;
    }
}
