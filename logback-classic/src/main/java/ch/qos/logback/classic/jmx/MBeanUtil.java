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
package ch.qos.logback.classic.jmx;

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

    static public String getObjectNameFor(String contextName, Class type) {
        return DOMAIN + ":Name=" + contextName + ",Type=" + type.getName();
    }

    public static ObjectName string2ObjectName(Context context, Object caller, String objectNameAsStr) {
        String msg = "Failed to convert [" + objectNameAsStr + "] to ObjectName";

        StatusUtil statusUtil = new StatusUtil(context);
        try {
            return new ObjectName(objectNameAsStr);
        } catch (MalformedObjectNameException e) {
            statusUtil.addError(caller, msg, e);
            return null;
        } catch (NullPointerException e) {
            statusUtil.addError(caller, msg, e);
            return null;
        }
    }

    public static boolean isRegistered(MBeanServer mbs, ObjectName objectName) {
        return mbs.isRegistered(objectName);
    }

    public static void createAndRegisterJMXConfigurator(MBeanServer mbs, LoggerContext loggerContext, JMXConfigurator jmxConfigurator, ObjectName objectName,
                    Object caller) {
        try {
            mbs.registerMBean(jmxConfigurator, objectName);
        } catch (Exception e) {
            StatusUtil statusUtil = new StatusUtil(loggerContext);
            statusUtil.addError(caller, "Failed to create mbean", e);
        }
    }

    public static void unregister(LoggerContext loggerContext, MBeanServer mbs, ObjectName objectName, Object caller) {

        StatusUtil statusUtil = new StatusUtil(loggerContext);
        if (mbs.isRegistered(objectName)) {
            try {
                statusUtil.addInfo(caller, "Unregistering mbean [" + objectName + "]");
                mbs.unregisterMBean(objectName);
            } catch (InstanceNotFoundException e) {
                // this is theoretically impossible
                statusUtil.addError(caller, "Failed to unregister mbean" + objectName, e);
            } catch (MBeanRegistrationException e) {
                // this is theoretically impossible
                statusUtil.addError(caller, "Failed to unregister mbean" + objectName, e);
            }
        } else {
            statusUtil.addInfo(caller, "mbean [" + objectName + "] does not seem to be registered");
        }
    }

}
