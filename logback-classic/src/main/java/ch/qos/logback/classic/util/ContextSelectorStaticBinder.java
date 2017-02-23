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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextJNDISelector;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.selector.DefaultContextSelector;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

/**
 * Holds the context selector for use in the current environment.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since 0.9.19
 */
public class ContextSelectorStaticBinder {

    static ContextSelectorStaticBinder singleton = new ContextSelectorStaticBinder();

    ContextSelector contextSelector;
    Object key;

    public static ContextSelectorStaticBinder getSingleton() {
        return singleton;
    }

    /**
     * FOR INTERNAL USE. This method is intended for use by  StaticLoggerBinder.
     *  
     * @param defaultLoggerContext
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public void init(LoggerContext defaultLoggerContext, Object key) throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
                    IllegalAccessException, InvocationTargetException {
        if (this.key == null) {
            this.key = key;
        } else if (this.key != key) {
            throw new IllegalAccessException("Only certain classes can access this method.");
        }

        String contextSelectorStr = OptionHelper.getSystemProperty(ClassicConstants.LOGBACK_CONTEXT_SELECTOR);
        if (contextSelectorStr == null) {
            contextSelector = new DefaultContextSelector(defaultLoggerContext);
        } else if (contextSelectorStr.equals("JNDI")) {
            // if jndi is specified, let's use the appropriate class
            contextSelector = new ContextJNDISelector(defaultLoggerContext);
        } else {
            contextSelector = dynamicalContextSelector(defaultLoggerContext, contextSelectorStr);
        }
    }

    /**
     * Instantiate the context selector class designated by the user. The selector
     * must have a constructor taking a LoggerContext instance as an argument.
     * 
     * @param defaultLoggerContext
     * @param contextSelectorStr
     * @return an instance of the designated context selector class
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    static ContextSelector dynamicalContextSelector(LoggerContext defaultLoggerContext, String contextSelectorStr) throws ClassNotFoundException,
                    SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException,
                    InvocationTargetException {
        Class<?> contextSelectorClass = Loader.loadClass(contextSelectorStr);
        Constructor cons = contextSelectorClass.getConstructor(new Class[] { LoggerContext.class });
        return (ContextSelector) cons.newInstance(defaultLoggerContext);
    }

    public ContextSelector getContextSelector() {
        return contextSelector;
    }

}
