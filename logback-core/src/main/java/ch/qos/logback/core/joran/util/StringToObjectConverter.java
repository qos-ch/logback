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

    private static final Class<?>[] STING_CLASS_PARAMETER = new Class[] { String.class };

    static public boolean canBeBuiltFromSimpleString(Class<?> parameterClass) {
        Package p = parameterClass.getPackage();
        if (parameterClass.isPrimitive()) {
            return true;
        } else if (p != null && "java.lang".equals(p.getName())) {
            return true;
        } else if (followsTheValueOfConvention(parameterClass)) {
            return true;
        } else if (parameterClass.isEnum()) {
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
    public static Object convertArg(ContextAware ca, String val, Class<?> type) {
        if (val == null) {
            return null;
        }
        String v = val.trim();
        if (String.class.isAssignableFrom(type)) {
            return v;
        } else if (Integer.TYPE.isAssignableFrom(type)) {
            return new Integer(v);
        } else if (Long.TYPE.isAssignableFrom(type)) {
            return new Long(v);
        } else if (Float.TYPE.isAssignableFrom(type)) {
            return new Float(v);
        } else if (Double.TYPE.isAssignableFrom(type)) {
            return new Double(v);
        } else if (Boolean.TYPE.isAssignableFrom(type)) {
            if ("true".equalsIgnoreCase(v)) {
                return Boolean.TRUE;
            } else if ("false".equalsIgnoreCase(v)) {
                return Boolean.FALSE;
            }
        } else if (type.isEnum()) {
            return convertToEnum(ca, v, (Class<? extends Enum>) type);
        } else if (StringToObjectConverter.followsTheValueOfConvention(type)) {
            return convertByValueOfMethod(ca, type, v);
        } else if (isOfTypeCharset(type)) {
            return convertToCharset(ca, val);
        }

        return null;
    }

    static private boolean isOfTypeCharset(Class<?> type) {
        return Charset.class.isAssignableFrom(type);
    }

    static private Charset convertToCharset(ContextAware ca, String val) {
        try {
            return Charset.forName(val);
        } catch (UnsupportedCharsetException e) {
            ca.addError("Failed to get charset [" + val + "]", e);
            return null;
        }
    }

    static private boolean followsTheValueOfConvention(Class<?> parameterClass) {
        try {
            Method valueOfMethod = parameterClass.getMethod(CoreConstants.VALUE_OF, STING_CLASS_PARAMETER);
            int mod = valueOfMethod.getModifiers();
            if (Modifier.isStatic(mod)) {
                return true;
            }
        } catch (SecurityException e) {
            // nop
        } catch (NoSuchMethodException e) {
            // nop
        }
        return false;
    }

    private static Object convertByValueOfMethod(ContextAware ca, Class<?> type, String val) {
        try {
            Method valueOfMethod = type.getMethod(CoreConstants.VALUE_OF, STING_CLASS_PARAMETER);
            return valueOfMethod.invoke(null, val);
        } catch (Exception e) {
            ca.addError("Failed to invoke " + CoreConstants.VALUE_OF + "{} method in class [" + type.getName() + "] with value [" + val + "]");
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Object convertToEnum(ContextAware ca, String val, Class<? extends Enum> enumType) {
        return Enum.valueOf(enumType, val);
    }

    boolean isBuildableFromSimpleString() {
        return false;
    }
}
