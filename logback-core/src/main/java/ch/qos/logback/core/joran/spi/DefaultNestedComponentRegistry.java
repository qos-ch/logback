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
package ch.qos.logback.core.joran.spi;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry which maps a property in a host class to a default class.
 *
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class DefaultNestedComponentRegistry {

	Map<HostClassAndPropertyDouble, Class<?>> defaultComponentMap = new HashMap<>();
	Map<String, Class<?>> tagToClassMap = new HashMap<>();

	public void add(final Class<?> hostClass, final String propertyName, final Class<?> componentClass) {
		final HostClassAndPropertyDouble hpDouble = new HostClassAndPropertyDouble(hostClass, propertyName.toLowerCase());
		defaultComponentMap.put(hpDouble, componentClass);
		tagToClassMap.put(propertyName, componentClass);
	}


	public String findDefaultComponentTypeByTag(final String tagName) {
		final Class<?> defaultClass = tagToClassMap.get(tagName);
		if(defaultClass == null) {
			return null;
		}
		return defaultClass.getCanonicalName();
	}

	public Class<?> findDefaultComponentType(Class<?> hostClass, String propertyName) {
		propertyName = propertyName.toLowerCase();
		while (hostClass != null) {
			final Class<?> componentClass = oneShotFind(hostClass, propertyName);
			if (componentClass != null) {
				return componentClass;
			}
			hostClass = hostClass.getSuperclass();
		}
		return null;
	}

	private Class<?> oneShotFind(final Class<?> hostClass, final String propertyName) {
		final HostClassAndPropertyDouble hpDouble = new HostClassAndPropertyDouble(hostClass, propertyName);
		return defaultComponentMap.get(hpDouble);
	}

}
