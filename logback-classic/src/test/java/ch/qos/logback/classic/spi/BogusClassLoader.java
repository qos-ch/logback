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
package ch.qos.logback.classic.spi;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A trivial class loader which throws a NoClassDefFoundError if the requested
 * class name contains the string "Bogus".
 * 
 * @author Ceki Gulcu
 */
public class BogusClassLoader extends URLClassLoader {

    public BogusClassLoader(URL[] urls) {
        super(urls);
    }

    public BogusClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    /**
     * Throw NoClassDefFoundError if the requested class contains the string
     * "Bogus". Otherwise, delegate to super-class.
     */
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        if (name.contains("Bogus")) {
            throw new NoClassDefFoundError();
        }

        return super.loadClass(name, resolve);
    }
}
