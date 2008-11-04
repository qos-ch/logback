/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.jmx;

import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.StatusUtil;

public class MBeanUtil {

  static final String DOMAIN = "ch.qos.logback.classic";

  static public String getObjectNameFor(Context context, Class type) {
    String objectNameAsStr = DOMAIN + ":Name=" + context.getName() + ",Type="
        + type.getName();
    return objectNameAsStr;
  }

  public static ObjectName string2ObjectName(Context context, Object caller,
      String objectNameAsStr) {
    String msg = "Failed to convert [" + objectNameAsStr + "] to ObjectName";

    try {
      return new ObjectName(objectNameAsStr);
    } catch (MalformedObjectNameException e) {
      StatusUtil.addError(context, caller, msg, e);
      return null;
    } catch (NullPointerException e) {
      StatusUtil.addError(context, caller, msg, e);
      return null;
    }
  }

  public static JMXConfigurator register(LoggerContext loggerContext,
      ObjectName objectName, Object caller) {
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

      JMXConfigurator jmxConfigurator = new JMXConfigurator(loggerContext,
          mbs, objectName);

      if (mbs.isRegistered(objectName)) {
        StatusUtil.addWarn(loggerContext, caller,
            "Unregistering existing MBean named ["
                + objectName.getCanonicalName() + "]");
        mbs.unregisterMBean(objectName);
      }
      mbs.registerMBean(jmxConfigurator, objectName);
      return jmxConfigurator;
    } catch (Exception e) {
      StatusUtil.addError(loggerContext, caller, "Failed to create mbean", e);
      return null;
    }
  }
  
 


  

  public static void unregister(LoggerContext loggerContext, MBeanServer mbs,
      ObjectName objectName, Object caller) {
    if (mbs.isRegistered(objectName)) {
      try {
        StatusUtil.addInfo(loggerContext, caller, "Unregistering mbean ["
            + objectName + "]");
        mbs.unregisterMBean(objectName);
      } catch (InstanceNotFoundException e) {
        // this is theoretically impossible
        e.printStackTrace();
      } catch (MBeanRegistrationException e) {
        // this also is theoretically impossible
        e.printStackTrace();
      }
    } else {
      StatusUtil.addInfo(loggerContext, caller, "mbean [" + objectName
          + "] does not seem to be registered");
    }
  }

}
