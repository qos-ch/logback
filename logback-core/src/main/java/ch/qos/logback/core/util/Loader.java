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
package ch.qos.logback.core.util;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;

import ch.qos.logback.core.Context;

/**
 * Load resources (or images) from various sources.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class Loader {
    static final String TSTR = "Caught Exception while in Loader.getResource. This may be innocuous.";

    private static boolean ignoreTCL = false;
    public static final String IGNORE_TCL_PROPERTY_NAME = "logback.ignoreTCL";
    private static boolean HAS_GET_CLASS_LOADER_PERMISSION = false;

    static {
        String ignoreTCLProp = OptionHelper.getSystemProperty(IGNORE_TCL_PROPERTY_NAME, null);

        if (ignoreTCLProp != null) {
            ignoreTCL = OptionHelper.toBoolean(ignoreTCLProp, true);
        }

        HAS_GET_CLASS_LOADER_PERMISSION = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                try {
                    AccessController.checkPermission(new RuntimePermission("getClassLoader"));
                    return true;
                } catch (SecurityException e) {
                    // Using SecurityException instead of AccessControlException.
                    // See bug LOGBACK-760.
                    return false;
                }
            }
        });
    }

    /**
     * Compute the number of occurrences a resource can be found by a class
     * loader.
     *
     * @param resource
     * @param classLoader
     * @return
     * @throws IOException
     */

    public static Set<URL> getResources(String resource, ClassLoader classLoader) throws IOException {
        // See LBCLASSIC-159
        Set<URL> urlSet = new HashSet<URL>();
        Enumeration<URL> urlEnum = classLoader.getResources(resource);
        while (urlEnum.hasMoreElements()) {
            URL url = urlEnum.nextElement();
            urlSet.add(url);
        }
        return urlSet;
    }

    /**
     * Search for a resource using the classloader passed as parameter.
     *
     * @param resource    the resource name to look for
     * @param classLoader the classloader used for the search
     */
    public static URL getResource(String resource, ClassLoader classLoader) {
        try {
            return classLoader.getResource(resource);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Attempt to find a resource by using the classloader that loaded this class,
     * namely Loader.class.
     *
     * @param resource
     * @return
     */
    public static URL getResourceBySelfClassLoader(String resource) {
        return getResource(resource, getClassLoaderOfClass(Loader.class));
    }

    // private static URL getResourceByTCL(String resource) {
    // return getResource(resource, getTCL());
    // }

    /**
     * Get the Thread Context Loader which is a JDK 1.2 feature. If we are running
     * under JDK 1.1 or anything else goes wrong the method returns
     * {@code null}.
     */
    public static ClassLoader getTCL() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static Class<?> loadClass(String clazz, Context context) throws ClassNotFoundException {
        ClassLoader cl = getClassLoaderOfObject(context);
        return cl.loadClass(clazz);
    }

    /**
     * Get the class loader of the object passed as argument. Return the system
     * class loader if appropriate.
     *
     * @param o
     * @return
     */
    public static ClassLoader getClassLoaderOfObject(Object o) {
        if (o == null) {
            throw new NullPointerException("Argument cannot be null");
        }
        return getClassLoaderOfClass(o.getClass());
    }

    /**
     * Returns the class loader of clazz in an access privileged section.
     *
     * @param clazz
     * @return
     */
    public static ClassLoader getClassLoaderAsPrivileged(final Class<?> clazz) {
        if (!HAS_GET_CLASS_LOADER_PERMISSION)
            return null;
        else
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    return clazz.getClassLoader();
                }
            });
    }

    /**
     * Return the class loader which loaded the class passed as argument. Return
     * the system class loader if appropriate.
     *
     * @param clazz
     * @return
     */
    public static ClassLoader getClassLoaderOfClass(final Class<?> clazz) {
        ClassLoader cl = clazz.getClassLoader();
        if (cl == null) {
            return ClassLoader.getSystemClassLoader();
        } else {
            return cl;
        }
    }

    /**
     * If running under JDK 1.2 load the specified class using the
     * <code>Thread</code> <code>contextClassLoader</code> if that fails try
     * Class.forname. Under JDK 1.1 only Class.forName is used.
     */
    public static Class<?> loadClass(String clazz) throws ClassNotFoundException {
        // Just call Class.forName(clazz) if we are running under JDK 1.1
        // or if we are instructed to ignore the TCL.
        if (ignoreTCL) {
            return Class.forName(clazz);
        } else {
            try {
                return getTCL().loadClass(clazz);
            } catch (Throwable e) {
                // we reached here because tcl was null or because of a
                // security exception, or because clazz could not be loaded...
                // In any case we now try one more time
                return Class.forName(clazz);
            }
        }
    }
}
