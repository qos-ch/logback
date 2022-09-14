/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
 * <p>
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 * <p>
 * or (per the licensee's choosing)
 * <p>
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Helper methods for method invocation via {@link java.lang.reflect} package.
 *
 * @since 1.3.1
 */
public class ReflectionUtil {

    public static Object invokeMethodOnObject(Object obj, String methodName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return invokeMethodOnObject(obj, methodName, null, null);
    }

    public static Object invokeMethodOnObject(Object obj, String methodName, Class[] paramTypes, Object[] params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Method method = obj.getClass().getMethod(methodName, paramTypes);
            return method.invoke(obj, params);
    }
}
