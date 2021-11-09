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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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

public class ContextInitializerTest {

	LoggerContext loggerContext = new LoggerContext();
	Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		System.clearProperty(ClassicConstants.CONFIG_FILE_PROPERTY);
		System.clearProperty(CoreConstants.STATUS_LISTENER_CLASS_KEY);
		MockConfigurator.context = null;
	}

	@Test
	@Ignore
	// this test works only if logback-test.xml or logback.xml files are on the classpath.
	// However, this is something we try to avoid in order to simplify the life
	// of users trying to follow the manual and logback-examples from an IDE
	public void reset() throws JoranException {
		{
			new ContextInitializer(loggerContext).autoConfig();
			final Appender<ILoggingEvent> appender = root.getAppender("STDOUT");
			assertNotNull(appender);
			assertTrue(appender instanceof ConsoleAppender);
		}
		{
			loggerContext.stop();
			final Appender<ILoggingEvent> appender = root.getAppender("STDOUT");
			assertNull(appender);
		}
	}

	@Test
	public void autoConfigFromSystemProperties() throws JoranException {
		doAutoConfigFromSystemProperties(ClassicTestConstants.INPUT_PREFIX + "autoConfig.xml");
		doAutoConfigFromSystemProperties("autoConfigAsResource.xml");
		// test passing a URL. note the relative path syntax with file:src/test/...
		doAutoConfigFromSystemProperties("file:" + ClassicTestConstants.INPUT_PREFIX + "autoConfig.xml");
	}

	public void doAutoConfigFromSystemProperties(final String val) throws JoranException {
		// lc.reset();
		System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, val);
		new ContextInitializer(loggerContext).autoConfig();
		final Appender<ILoggingEvent> appender = root.getAppender("AUTO_BY_SYSTEM_PROPERTY");
		assertNotNull(appender);
	}

	@Test
	public void autoConfigFromServiceLoaderJDK6andAbove() throws Exception {
		assumeTrue(!isJDK5());
		setupMockServiceLoader();
		assertNull(MockConfigurator.context);
		new ContextInitializer(loggerContext).autoConfig();
		assertNotNull(MockConfigurator.context);
		assertSame(loggerContext, MockConfigurator.context);
	}

	@Test
	public void autoConfigFromServiceLoaderJDK5() throws Exception {
		assumeTrue(isJDK5());
		setupMockServiceLoader();
		assertNull(MockConfigurator.context);
		new ContextInitializer(loggerContext).autoConfig();
		assertNull(MockConfigurator.context);
	}

	@Test
	public void autoStatusListener() throws JoranException {
		System.setProperty(CoreConstants.STATUS_LISTENER_CLASS_KEY, TrivialStatusListener.class.getName());
		List<StatusListener> statusListenerList = loggerContext.getStatusManager().getCopyOfStatusListenerList();
		assertEquals(0, statusListenerList.size());
		doAutoConfigFromSystemProperties(ClassicTestConstants.INPUT_PREFIX + "autoConfig.xml");
		statusListenerList = loggerContext.getStatusManager().getCopyOfStatusListenerList();
		assertTrue(statusListenerList.size() + " should be 1", statusListenerList.size() == 1);
		// LOGBACK-767
		final TrivialStatusListener tsl = (TrivialStatusListener) statusListenerList.get(0);
		assertTrue("expecting at least one event in list", tsl.list.size() > 0);
	}

	@Test
	public void autoOnConsoleStatusListener() throws JoranException {
		System.setProperty(CoreConstants.STATUS_LISTENER_CLASS_KEY, CoreConstants.SYSOUT);
		List<StatusListener> sll = loggerContext.getStatusManager().getCopyOfStatusListenerList();
		assertEquals(0, sll.size());
		doAutoConfigFromSystemProperties(ClassicTestConstants.INPUT_PREFIX + "autoConfig.xml");
		sll = loggerContext.getStatusManager().getCopyOfStatusListenerList();
		assertTrue(sll.size() + " should be 1", sll.size() == 1);
	}

	@Test
	public void shouldConfigureFromXmlFile() throws MalformedURLException, JoranException {
		final LoggerContext loggerContext = new LoggerContext();
		final ContextInitializer initializer = new ContextInitializer(loggerContext);
		assertNull(loggerContext.getObject(CoreConstants.SAFE_JORAN_CONFIGURATION));

		final URL configurationFileUrl = Loader.getResource("BOO_logback-test.xml", Thread.currentThread().getContextClassLoader());
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
		final LoggerContext loggerContext = new LoggerContext();
		final ContextInitializer initializer = new ContextInitializer(loggerContext);

		final URL configurationFileUrl = Loader.getResource("README.txt", Thread.currentThread().getContextClassLoader());
		try {
			initializer.configureByResource(configurationFileUrl);
			fail("Should throw LogbackException");
		} catch (final LogbackException expectedException) {
			// pass
		}
	}

	private static boolean isJDK5() {
		final String ver = System.getProperty("java.version");
		return ver.startsWith("1.5.") || ver.equals("1.5");
	}

	private void setupMockServiceLoader() {
		final ClassLoader realLoader = ClassicEnvUtil.class.getClassLoader();
		ClassicEnvUtil.testServiceLoaderClassLoader = new WrappedClassLoader(realLoader) {

			@Override
			public Enumeration<URL> getResources(final String name) throws IOException {
				final Enumeration<URL> r;
				if (name.endsWith("META-INF/services/ch.qos.logback.classic.spi.Configurator")) {
					final Vector<URL> vs = new Vector<>();
					final URL u = super.getResource("FAKE_META_INF_SERVICES_ch_qos_logback_classic_spi_Configurator");
					vs.add(u);
					return vs.elements();
				}
				return super.getResources(name);
			}
		};
	}

	static class WrappedClassLoader extends ClassLoader {
		final ClassLoader delegate;

		public WrappedClassLoader(final ClassLoader delegate) {
			this.delegate = delegate;
		}

		@Override
		public Class<?> loadClass(final String name) throws ClassNotFoundException {
			return delegate.loadClass(name);
		}

		@Override
		public URL getResource(final String name) {
			return delegate.getResource(name);
		}

		@Override
		public Enumeration<URL> getResources(final String name) throws IOException {
			return delegate.getResources(name);
		}

		@Override
		public InputStream getResourceAsStream(final String name) {
			return delegate.getResourceAsStream(name);
		}

		@Override
		public void setDefaultAssertionStatus(final boolean enabled) {
			delegate.setDefaultAssertionStatus(enabled);
		}

		@Override
		public void setPackageAssertionStatus(final String packageName, final boolean enabled) {
			delegate.setPackageAssertionStatus(packageName, enabled);
		}

		@Override
		public void setClassAssertionStatus(final String className, final boolean enabled) {
			delegate.setClassAssertionStatus(className, enabled);
		}

		@Override
		public void clearAssertionStatus() {
			delegate.clearAssertionStatus();
		}
	}

}
