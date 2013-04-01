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
 * Register a component factory with the logback context to be used to instantiate various objects.
 * 
 * @author Rich Mayfield
 */
public interface ComponentFactory {

    /**
     * Creates an instance of the named class.
     * 
     * @param className
     *            The name of the class to instantiate
     * @return An instance of the class
     * @throws DynamicClassLoadingException
     *             If the component knows about the desired class but experiences some other error
     *             instantiating an object.
     * @throws ClassNotFoundException
     *             If this component factory knows nothing about this class.
     */

    Object getInstance(String className) throws DynamicClassLoadingException,
            ClassNotFoundException;
}
