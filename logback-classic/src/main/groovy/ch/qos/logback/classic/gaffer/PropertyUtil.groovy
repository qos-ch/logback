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
package ch.qos.logback.classic.gaffer

import java.lang.reflect.Method

import com.sun.org.apache.xpath.internal.axes.SubContextList;

import ch.qos.logback.core.joran.util.StringToObjectConverter;
import ch.qos.logback.core.joran.util.beans.BeanUtil

/**
 * @author Ceki G&uuml;c&uuml;
 */
class PropertyUtil {

	static boolean hasAdderMethod(final Object obj, final String name) {
		final String addMethod = "add${upperCaseFirstLetter(name)}";
		return obj.metaClass.respondsTo(obj, addMethod);
	}


	static NestingType nestingType(final Object obj, final String name, final Object value) {
		final def decapitalizedName = BeanUtil.toLowerCamelCase(name);
		final MetaProperty metaProperty = obj.hasProperty(decapitalizedName);

		if(metaProperty != null) {
			final boolean VALUE_IS_A_STRING = value instanceof String;

			if(VALUE_IS_A_STRING && StringToObjectConverter.followsTheValueOfConvention(metaProperty.getType())) {
				return NestingType.SINGLE_WITH_VALUE_OF_CONVENTION;
			}
			return NestingType.SINGLE;
		}
		if (hasAdderMethod(obj, name)) {
			return NestingType.AS_COLLECTION;
		}
		return NestingType.NA;
	}

	static Object convertByValueMethod(final Object component, final String name, final String value) {
		final def decapitalizedName = BeanUtil.toLowerCamelCase(name);
		final MetaProperty metaProperty = component.hasProperty(decapitalizedName);
		final Method valueOfMethod = StringToObjectConverter.getValueOfMethod(metaProperty.getType());
		return valueOfMethod.invoke(null, value);
	}

	static void attach(final NestingType nestingType, final Object component, final Object subComponent, String name) {
		switch (nestingType) {
		case NestingType.SINGLE_WITH_VALUE_OF_CONVENTION:
			name = BeanUtil.toLowerCamelCase(name);
			final Object value = convertByValueMethod(component, name, subComponent);
			component."${name}" = value;
			break;
		case NestingType.SINGLE:
			name = BeanUtil.toLowerCamelCase(name);
			component."${name}" = subComponent;
			break;

		case NestingType.AS_COLLECTION:
			final String firstUpperName = PropertyUtil.upperCaseFirstLetter(name)
			component."add${firstUpperName}"(subComponent);
			break;
		}
	}

	static String transformFirstLetter(final String s, final Closure closure) {
		if (s == null || s.length() == 0) {
			return s;
		}

		final String firstLetter = new String(s.getAt(0));

		final String modifiedFistLetter = closure(firstLetter);

		if (s.length() == 1)
			return modifiedFistLetter
					return modifiedFistLetter + s.substring(1);
	}

	static String upperCaseFirstLetter(final String s) {
		return transformFirstLetter(s, {String it -> it.toUpperCase()})
	}
}
