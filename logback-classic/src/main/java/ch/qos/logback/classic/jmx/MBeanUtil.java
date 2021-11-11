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

    static public String getObjectNameFor(final String contextName, final Class<?> type) {
        return DOMAIN + ":Name=" + contextName + ",Type=" + type.getName();
    }

    public static ObjectName string2ObjectName(final Context context, final Object caller, final String objectNameAsStr) {
        final String msg = "Failed to convert [" + objectNameAsStr + "] to ObjectName";

        final StatusUtil statusUtil = new StatusUtil(context);
        try {
            return new ObjectName(objectNameAsStr);
        } catch (final MalformedObjectNameException | NullPointerException e) {
            statusUtil.addError(caller, msg, e);
            return null;
        }
    }

    public static boolean isRegistered(final MBeanServer mbs, final ObjectName objectName) {
        return mbs.isRegistered(objectName);
    }

    public static void createAndRegisterJMXConfigurator(final MBeanServer mbs, final LoggerContext loggerContext, final JMXConfigurator jmxConfigurator,
                    final ObjectName objectName, final Object caller) {
        try {
            mbs.registerMBean(jmxConfigurator, objectName);
        } catch (final Exception e) {
            final StatusUtil statusUtil = new StatusUtil(loggerContext);
            statusUtil.addError(caller, "Failed to create mbean", e);
        }
    }

    public static void unregister(final LoggerContext loggerContext, final MBeanServer mbs, final ObjectName objectName, final Object caller) {

        final StatusUtil statusUtil = new StatusUtil(loggerContext);
        if (mbs.isRegistered(objectName)) {
            try {
                statusUtil.addInfo(caller, "Unregistering mbean [" + objectName + "]");
                mbs.unregisterMBean(objectName);
            } catch (final InstanceNotFoundException | MBeanRegistrationException e) {
                // this is theoretically impossible
                statusUtil.addError(caller, "Failed to unregister mbean" + objectName, e);
            }
        } else {
            statusUtil.addInfo(caller, "mbean [" + objectName + "] does not seem to be registered");
        }
    }

}
