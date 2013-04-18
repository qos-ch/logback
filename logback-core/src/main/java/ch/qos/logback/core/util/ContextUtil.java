/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.ContextAwareBase;

public class ContextUtil extends ContextAwareBase {

  public ContextUtil(Context context) {
    setContext(context);
  }

  public static String getLocalHostName() throws UnknownHostException {
    try {
      InetAddress localhost = InetAddress.getLocalHost();
      return localhost.getHostName();
    } catch (UnknownHostException e) {
      String ipAddress = getLocalAddressAsString();
      if (ipAddress == null) {
        throw e;
      }
      return ipAddress;
    }
  }

  private static String getLocalAddressAsString() {
    Enumeration<NetworkInterface> interfaces = getNetworkInterfaces();
    if (interfaces == null) {
      return null;
    }

    while (interfaces.hasMoreElements()) {
      NetworkInterface networkInterface = interfaces.nextElement();
      Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
      while(inetAddresses.hasMoreElements()) {
        InetAddress ipAddress = inetAddresses.nextElement();
        if (invalidAddress(ipAddress)) {
          continue;
        }
        return ipAddress.getHostAddress();
      }
    }
    return null;
  }

  private static Enumeration<NetworkInterface> getNetworkInterfaces() {
    Enumeration<NetworkInterface> interfaces;
    try {
      interfaces = NetworkInterface.getNetworkInterfaces();
    }
    catch (SocketException e) {
      return null;
    }
    return interfaces;
  }

  private static boolean invalidAddress(InetAddress ipAddress) {
    return ipAddress == null
      || ipAddress.isLoopbackAddress()
      || ipAddress.isAnyLocalAddress()
      || ipAddress.isLinkLocalAddress()
      || ipAddress.isMulticastAddress();
  }

  /**
   * Add the local host's name as a property
   */
  public void addHostNameAsProperty() {
    try {
      String localhostName =  getLocalHostName();
      context.putProperty(CoreConstants.HOSTNAME_KEY, localhostName);
    } catch (UnknownHostException e) {
      addError("Failed to get local hostname", e);
    } catch (SecurityException e) {
      addError("Failed to get local hostname", e);
    }
  }

   public void addProperties(Properties props) {
    if (props == null) {
      return;
    }
    Iterator i = props.keySet().iterator();
    while (i.hasNext()) {
      String key = (String) i.next();
      context.putProperty(key, props.getProperty(key));
    }
  }


  public void addGroovyPackages(List<String> frameworkPackages) {
    //addFrameworkPackage(frameworkPackages, "groovy.lang");
    addFrameworkPackage(frameworkPackages, "org.codehaus.groovy.runtime");
  }

  public void addFrameworkPackage(List<String> frameworkPackages, String packageName) {
    if(!frameworkPackages.contains(packageName)) {
      frameworkPackages.add(packageName);
    }
  }


}
