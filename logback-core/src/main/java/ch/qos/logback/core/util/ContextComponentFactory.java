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

import ch.qos.logback.core.Context;

/**
 * Basic implementation of a ComponentFactory that merely uses the context's classloader to
 * resolve and create instances of classes.
 * 
 * @author Rich Mayfield
 * 
 */
public class ContextComponentFactory extends ClassLoaderComponentFactory {

    public ContextComponentFactory(Context context) {
        super(Loader.getClassLoaderOfObject(context));
    }
}
