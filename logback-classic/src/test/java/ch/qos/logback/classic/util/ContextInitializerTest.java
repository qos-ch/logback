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
package ch.qos.logback.classic.util;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.testUtil.TrivialStatusListener;
import ch.qos.logback.core.util.Loader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ContextInitializerTest {

    static final String PATH_TO_META_INF_CONF_SERVICE = "META-INF/services/ch.qos.logback.classic.spi.Configurator";
    static final String FAKE_META_INF_SERVICES = "FAKE_META_INF_SERVICES_ch_qos_logback_classic_spi_Configurator";
    LoggerContext loggerContext = new LoggerContext();
    Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
        System.clearProperty(ClassicConstants.CONFIG_FILE_PROPERTY);
        System.clearProperty(CoreConstants.STATUS_LISTENER_CLASS_KEY);
        //ClassicEnvUtil.testServiceLoaderClassLoader = null;
        MockConfigurator.context = null;
    }

    @Test
    @Disabled
    // this test works only if logback-test.xml or logback.xml files are on the
    // classpath.
    // However, this is something we try to avoid in order to simplify the life
    // of users trying to follow the manual and logback-examples from an IDE
    public void reset() throws JoranException {
        {
            new ContextInitializer(loggerContext).autoConfig();
            Appender<ILoggingEvent> appender = root.getAppender("STDOUT");
            assertNotNull(appender);
            assertTrue(appender instanceof ConsoleAppender);
        }
        {
            loggerContext.stop();
            Appender<ILoggingEvent> appender = root.getAppender("STDOUT");
            assertNull(appender);
        }
    }

    @Test
    public void autoConfigFromSystemProperties() throws JoranException {
        doAutoConfigFromSystemProperties(ClassicTestConstants.INPUT_PREFIX + "autoConfig.xml");
        doAutoConfigFromSystemProperties("autoConfigAsResource.xml");
        // test passing a URL. note the relative path syntax with file:src/test/...
        doAutoConfigFromSystemProperties("file://./" + ClassicTestConstants.INPUT_PREFIX + "autoConfig.xml");
    }

    public void doAutoConfigFromSystemProperties(String val) throws JoranException {
        // lc.reset();
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, val);
        new ContextInitializer(loggerContext).autoConfig();
        Appender<ILoggingEvent> appender = root.getAppender("AUTO_BY_SYSTEM_PROPERTY");
        assertNotNull(appender);
    }


    // this test as constructed cannot run in a modular environment since
    // ServiceLoader will not honor providers specified in a provider-configuration file (META-INF/..)
    // if module-info.java in the same module declares a provider
    //
    // https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/util/ServiceLoader.html#
    //
    //In a provider-configuration file, any mention of a service provider that is deployed
    // in a named module is ignored. This is to avoid duplicates that would otherwise arise
    // when a named module has both a provides directive and a provider-configuration file
    // that mention the same service provider.
    @Disabled
    @Test
    public void autoConfigFromServiceLoaderJDK6andAbove() throws Exception {
        assumeTrue(!isJDK5());
        ClassLoader mockClassLoader = buildMockServiceLoader(this.getClass().getClassLoader());
        assertNull(MockConfigurator.context);
        new ContextInitializer(loggerContext).autoConfig(mockClassLoader);
        assertNotNull(MockConfigurator.context);
        assertSame(loggerContext, MockConfigurator.context);
    }

    @Test
    public void autoConfigFromServiceLoaderJDK5() throws Exception {
        assumeTrue(isJDK5());
        ClassLoader mockClassLoader = buildMockServiceLoader(this.getClass().getClassLoader());
        assertNull(MockConfigurator.context);
        new ContextInitializer(loggerContext).autoConfig(mockClassLoader);
        assertNull(MockConfigurator.context);
    }

    @Test
    public void autoStatusListener() throws JoranException {
        System.setProperty(CoreConstants.STATUS_LISTENER_CLASS_KEY, TrivialStatusListener.class.getName());
        List<StatusListener> statusListenerList = loggerContext.getStatusManager().getCopyOfStatusListenerList();
        assertEquals(0, statusListenerList.size());
        doAutoConfigFromSystemProperties(ClassicTestConstants.INPUT_PREFIX + "autoConfig.xml");
        statusListenerList = loggerContext.getStatusManager().getCopyOfStatusListenerList();
        assertTrue( statusListenerList.size() == 1, statusListenerList.size() + " should be 1");
        // LOGBACK-767
        TrivialStatusListener tsl = (TrivialStatusListener) statusListenerList.get(0);
        assertTrue( tsl.list.size() > 0, "expecting at least one event in list");
    }

    @Test
    public void autoOnConsoleStatusListener() throws JoranException {
        System.setProperty(CoreConstants.STATUS_LISTENER_CLASS_KEY, CoreConstants.SYSOUT);
        List<StatusListener> sll = loggerContext.getStatusManager().getCopyOfStatusListenerList();
        assertEquals(0, sll.size());
        doAutoConfigFromSystemProperties(ClassicTestConstants.INPUT_PREFIX + "autoConfig.xml");
        sll = loggerContext.getStatusManager().getCopyOfStatusListenerList();
        assertTrue(sll.size() == 1, sll.size() + " should be 1");
    }

    @Test
    public void shouldConfigureFromXmlFile() throws MalformedURLException, JoranException {
        LoggerContext loggerContext = new LoggerContext();
        ContextInitializer initializer = new ContextInitializer(loggerContext);
        assertNull(loggerContext.getObject(CoreConstants.SAFE_JORAN_CONFIGURATION));

        URL configurationFileUrl = Loader.getResource("BOO_logback-test.xml",
                Thread.currentThread().getContextClassLoader());
        initializer.configureByResource(configurationFileUrl);

        assertNotNull(loggerContext.getObject(CoreConstants.SAFE_JORAN_CONFIGURATION));
    }

//    @Test
//    public void shouldConfigureFromGroovyScript() throws MalformedURLException, JoranException {
//        LoggerContext loggerContext = new LoggerContext();
//        ContextInitializer initializer = new ContextInitializer(loggerContext);
//        assertNull(loggerContext.getObject(CoreConstants.CONFIGURATION_WATCH_LIST));
//
//        URL configurationFileUrl = Loader.getResource("test.groovy", Thread.currentThread().getContextClassLoader());
//        initializer.configureByResource(configurationFileUrl);
//
//        assertNotNull(loggerContext.getObject(CoreConstants.CONFIGURATION_WATCH_LIST));
//    }

    @Test
    public void shouldThrowExceptionIfUnexpectedConfigurationFileExtension() throws JoranException {
        LoggerContext loggerContext = new LoggerContext();
        ContextInitializer initializer = new ContextInitializer(loggerContext);

        URL configurationFileUrl = Loader.getResource("README.txt", Thread.currentThread().getContextClassLoader());
        try {
            initializer.configureByResource(configurationFileUrl);
            fail("Should throw LogbackException");
        } catch (LogbackException expectedException) {
            // pass
        }
    }

    private static boolean isJDK5() {
        String ver = System.getProperty("java.version");
        boolean jdk5 = ver.startsWith("1.5.") || ver.equals("1.5");
        return jdk5;
    }

    private ClassLoader buildMockServiceLoader(ClassLoader realLoader) {


        //final ClassLoader realLoader = ClassicEnvUtil.class.getClassLoader();
        ClassLoader wrapperClassLoader = new WrappedClassLoader(realLoader) {

            @Override
            public String toString() {
                return "wrapperClassLoader: " +super.toString();
            }

            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                final Enumeration<URL> r;
                if (name.endsWith(PATH_TO_META_INF_CONF_SERVICE)) {
                    System.out.println("Hit on "+PATH_TO_META_INF_CONF_SERVICE);
                    Vector<URL> vs = new Vector<URL>();
                    URL u = super.getResource(FAKE_META_INF_SERVICES);
                    Assertions.assertNotNull(u);
                    System.out.println("Found url: "+u);
                    vs.add(u);
                    return vs.elements();
                } else {
                    r = super.getResources(name);
                }
                return r;
            }
        };

        return wrapperClassLoader;
    }

    static class WrappedClassLoader extends ClassLoader {
        final ClassLoader delegate;

        public WrappedClassLoader(ClassLoader delegate) {
            super();
            this.delegate = delegate;
        }

        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return delegate.loadClass(name);
        }

        public URL getResource(String name) {
            return delegate.getResource(name);
        }

        public Enumeration<URL> getResources(String name) throws IOException {
            return delegate.getResources(name);
        }

        public InputStream getResourceAsStream(String name) {
            return delegate.getResourceAsStream(name);
        }

        public void setDefaultAssertionStatus(boolean enabled) {
            delegate.setDefaultAssertionStatus(enabled);
        }

        public void setPackageAssertionStatus(String packageName, boolean enabled) {
            delegate.setPackageAssertionStatus(packageName, enabled);
        }

        public void setClassAssertionStatus(String className, boolean enabled) {
            delegate.setClassAssertionStatus(className, enabled);
        }

        public void clearAssertionStatus() {
            delegate.clearAssertionStatus();
        }
    }

}
