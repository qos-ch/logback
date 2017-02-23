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

import static ch.qos.logback.core.CoreConstants.FA_FILENAME_COLLISION_MAP;
import static ch.qos.logback.core.CoreConstants.RFA_FILENAME_PATTERN_COLLISION_MAP;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.spi.ContextAwareBase;

public class ContextUtil extends ContextAwareBase {

    public ContextUtil(Context context) {
        setContext(context);
    }

    public static String getLocalHostName() throws UnknownHostException, SocketException {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            return localhost.getHostName();
        } catch (UnknownHostException e) {
            return getLocalAddressAsString();
        }
    }

    private static String getLocalAddressAsString() throws UnknownHostException, SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces != null && interfaces.hasMoreElements()) {
            Enumeration<InetAddress> addresses = interfaces.nextElement().getInetAddresses();
            while (addresses != null && addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (acceptableAddress(address)) {
                    return address.getHostAddress();
                }
            }
        }
        throw new UnknownHostException();
    }

    private static boolean acceptableAddress(InetAddress address) {
        return address != null && !address.isLoopbackAddress() && !address.isAnyLocalAddress() && !address.isLinkLocalAddress();
    }

    /**
     * Add the local host's name as a property
     */
    public String safelyGetLocalHostName() {
        try {
            String localhostName = getLocalHostName();
            return localhostName;
        } catch (UnknownHostException e) {
            addError("Failed to get local hostname", e);
        } catch (SocketException e) {
            addError("Failed to get local hostname", e);
        } catch (SecurityException e) {
            addError("Failed to get local hostname", e);
        }
        return CoreConstants.UNKNOWN_LOCALHOST;
    }

    public void addProperties(Properties props) {
        if (props == null) {
            return;
        }
        @SuppressWarnings("rawtypes")
        Iterator i = props.keySet().iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            context.putProperty(key, props.getProperty(key));
        }
    }

    public static Map<String, String> getFilenameCollisionMap(Context context) {
        if (context == null)
            return null;
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map<String, String>) context.getObject(FA_FILENAME_COLLISION_MAP);
        return map;
    }

    public static Map<String, FileNamePattern> getFilenamePatternCollisionMap(Context context) {
        if (context == null)
            return null;
        @SuppressWarnings("unchecked")
        Map<String, FileNamePattern> map = (Map<String, FileNamePattern>) context.getObject(RFA_FILENAME_PATTERN_COLLISION_MAP);
        return map;
    }
    
    public void addGroovyPackages(List<String> frameworkPackages) {
        // addFrameworkPackage(frameworkPackages, "groovy.lang");
        addFrameworkPackage(frameworkPackages, "org.codehaus.groovy.runtime");
    }

    public void addFrameworkPackage(List<String> frameworkPackages, String packageName) {
        if (!frameworkPackages.contains(packageName)) {
            frameworkPackages.add(packageName);
        }
    }

}
