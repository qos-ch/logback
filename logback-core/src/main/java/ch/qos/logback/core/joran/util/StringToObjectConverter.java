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
package ch.qos.logback.core.joran.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.ContextAware;

/**
 * Utility class which can convert string into objects.
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class StringToObjectConverter {

	private static final Class<?>[] STRING_CLASS_PARAMETER = new Class[] { String.class };

	static public boolean canBeBuiltFromSimpleString(final Class<?> parameterClass) {
		final Package p = parameterClass.getPackage();
		if (parameterClass.isPrimitive()) {
			return true;
		}
		if ((p != null && "java.lang".equals(p.getName())) || followsTheValueOfConvention(parameterClass) || parameterClass.isEnum()) {
			return true;
		} else if (isOfTypeCharset(parameterClass)) {
			return true;
		}
		return false;
	}

	/**
	 * Convert <code>val</code> a String parameter to an object of a given type.
	 */
	@SuppressWarnings("unchecked")
	public static Object convertArg(final ContextAware ca, final String val, final Class<?> type) {
		if (val == null) {
			return null;
		}
		final String v = val.trim();
		if (String.class.isAssignableFrom(type)) {
			return v;
		}
		if (Integer.TYPE.isAssignableFrom(type)) {
			return Integer.valueOf(v);
		} else if (Long.TYPE.isAssignableFrom(type)) {
			return Long.valueOf(v);
		} else if (Float.TYPE.isAssignableFrom(type)) {
			return Float.valueOf(v);
		} else if (Double.TYPE.isAssignableFrom(type)) {
			return Double.valueOf(v);
		} else if (Boolean.TYPE.isAssignableFrom(type)) {
			if ("true".equalsIgnoreCase(v)) {
				return Boolean.TRUE;
			} else if ("false".equalsIgnoreCase(v)) {
				return Boolean.FALSE;
			}
		} else if (type.isEnum()) {
			return convertToEnum(ca, v, (Class<? extends Enum<?>>) type);
		} else if (StringToObjectConverter.followsTheValueOfConvention(type)) {
			return convertByValueOfMethod(ca, type, v);
		} else if (isOfTypeCharset(type)) {
			return convertToCharset(ca, val);
		}

		return null;
	}

	static private boolean isOfTypeCharset(final Class<?> type) {
		return Charset.class.isAssignableFrom(type);
	}

	static private Charset convertToCharset(final ContextAware ca, final String val) {
		try {
			return Charset.forName(val);
		} catch (final UnsupportedCharsetException e) {
			ca.addError("Failed to get charset [" + val + "]", e);
			return null;
		}
	}

	// returned value may be null and in most cases it is null.
	public static Method getValueOfMethod(final Class<?> type) {
		try {
			return type.getMethod(CoreConstants.VALUE_OF, STRING_CLASS_PARAMETER);
		} catch (final NoSuchMethodException | SecurityException e) {
			return null;
		}
	}

	static private boolean followsTheValueOfConvention(final Class<?> parameterClass) {
		final Method valueOfMethod = getValueOfMethod(parameterClass);
		if (valueOfMethod == null) {
			return false;
		}

		final int mod = valueOfMethod.getModifiers();
		return Modifier.isStatic(mod);
	}

	private static Object convertByValueOfMethod(final ContextAware ca, final Class<?> type, final String val) {
		try {
			final Method valueOfMethod = type.getMethod(CoreConstants.VALUE_OF, STRING_CLASS_PARAMETER);
			return valueOfMethod.invoke(null, val);
		} catch (final Exception e) {
			ca.addError("Failed to invoke " + CoreConstants.VALUE_OF + "{} method in class [" + type.getName() + "] with value [" + val + "]");
			return null;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object convertToEnum(final ContextAware ca, final String val, final Class<? extends Enum> enumType) {
		return Enum.valueOf(enumType, val);
	}

	boolean isBuildableFromSimpleString() {
		return false;
	}
}
