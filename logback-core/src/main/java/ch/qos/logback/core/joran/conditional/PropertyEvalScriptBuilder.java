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
package ch.qos.logback.core.joran.conditional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.spi.PropertyContainer;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ClassBodyEvaluator;

import ch.qos.logback.core.spi.ContextAwareBase;

public class PropertyEvalScriptBuilder extends ContextAwareBase {

    private static String SCRIPT_PREFIX = "" + "public boolean evaluate() { return ";
    private static String SCRIPT_SUFFIX = "" + "; }";

    final PropertyContainer localPropContainer;

    PropertyEvalScriptBuilder(PropertyContainer localPropContainer) {
        this.localPropContainer = localPropContainer;
    }

    Map<String, String> map = new HashMap<String, String>();

    public Condition build(String script) throws IllegalAccessException, CompileException, InstantiationException, SecurityException, NoSuchMethodException,
                    IllegalArgumentException, InvocationTargetException {

        ClassBodyEvaluator cbe = new ClassBodyEvaluator();
        cbe.setImplementedInterfaces(new Class[] { Condition.class });
        cbe.setExtendedClass(PropertyWrapperForScripts.class);
        cbe.setParentClassLoader(ClassBodyEvaluator.class.getClassLoader());
        cbe.cook(SCRIPT_PREFIX + script + SCRIPT_SUFFIX);

        Class<?> clazz = cbe.getClazz();
        Condition instance = (Condition) clazz.newInstance();
        Method setMapMethod = clazz.getMethod("setPropertyContainers", PropertyContainer.class, PropertyContainer.class);
        setMapMethod.invoke(instance, localPropContainer, context);

        return instance;
    }

}
