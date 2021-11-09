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

import java.util.Iterator;
import java.util.ServiceLoader;

import ch.qos.logback.core.util.Loader;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class ClassicEnvUtil {

	/*
	 * Used to replace the ClassLoader that the ServiceLoader uses for unit testing. We need this to mock the resources
	 * the ServiceLoader attempts to load from /META-INF/services thus keeping the projects src/test/resources clean
	 * (see src/test/resources/README.txt).
	 */
	static ClassLoader testServiceLoaderClassLoader = null;

	static public boolean isGroovyAvailable() {
		final ClassLoader classLoader = Loader.getClassLoaderOfClass(ClassicEnvUtil.class);
		try {
			final Class<?> bindingClass = classLoader.loadClass("groovy.lang.Binding");
			return bindingClass != null;
		} catch (final ClassNotFoundException e) {
			return false;
		}
	}

	private static ClassLoader getServiceLoaderClassLoader() {
		return testServiceLoaderClassLoader == null ? Loader.getClassLoaderOfClass(ClassicEnvUtil.class) : testServiceLoaderClassLoader;
	}

	public static <T> T loadFromServiceLoader(final Class<T> c) {
		final ServiceLoader<T> loader = ServiceLoader.load(c, getServiceLoaderClassLoader());
		final Iterator<T> it = loader.iterator();
		if (it.hasNext()) {
			return it.next();
		}
		return null;
	}

}
