/**
 * Logback: the reliable, generic, fast and flexible logging framework. Copyright (C) 1999-2011,
 * QOS.ch. All rights reserved.
 * 
 * This program and the accompanying materials are dual-licensed under either the terms of the
 * Eclipse Public License v1.0 as published by the Eclipse Foundation
 * 
 * or (per the licensee's choosing)
 * 
 * under the terms of the GNU Lesser General Public License version 2.1 as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.util;

/**
 * Basic implementation of a ComponentFactory that uses a classloader to resolve and create
 * instances of classes.
 * 
 * @author Rich Mayfield
 */
public class ClassLoaderComponentFactory implements ComponentFactory {

    private final ClassLoader classLoader;

    public ClassLoaderComponentFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;

    }

    /**
     * 
     * @see ch.qos.logback.core.util.ComponentFactory#getInstance(java.lang.String, java.lang.Class,
     *      java.lang.Object)
     */
    @Override
    public Object getInstance(String className) throws DynamicClassLoadingException,
            ClassNotFoundException {

        try {
            // ClassLoader classLoader = Loader.getClassLoaderOfObject(context);

            Class classObj = classLoader.loadClass(className);

            return classObj.newInstance();
        } catch (ClassNotFoundException e) {
            throw e;
        } catch (Throwable t) {
            throw new DynamicClassLoadingException("Failed to instantiate type " + className, t);
        }
    }

}
