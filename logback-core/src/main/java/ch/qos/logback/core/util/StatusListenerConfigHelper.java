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

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.StatusListener;

public class StatusListenerConfigHelper {

    public static void installIfAsked(Context context) {
        String slClass = OptionHelper.getSystemProperty(CoreConstants.STATUS_LISTENER_CLASS);
        if (!OptionHelper.isEmpty(slClass)) {
            addStatusListener(context, slClass);
        }
    }

    private static void addStatusListener(Context context, String listenerClassName) {
        StatusListener listener = null;
        if (CoreConstants.SYSOUT.equalsIgnoreCase(listenerClassName)) {
            listener = new OnConsoleStatusListener();
        } else {
            listener = createListenerPerClassName(context, listenerClassName);
        }
        initAndAddListener(context, listener);
    }

    private static void initAndAddListener(Context context, StatusListener listener) {
        if (listener != null) {
            if (listener instanceof ContextAware) // LOGBACK-767
                ((ContextAware) listener).setContext(context);

            boolean effectivelyAdded = context.getStatusManager().add(listener);
            effectivelyAdded = true;
            if (effectivelyAdded && (listener instanceof LifeCycle)) {
                ((LifeCycle) listener).start(); // LOGBACK-767
            }
        }
    }

    private static StatusListener createListenerPerClassName(Context context, String listenerClass) {
        try {
            return (StatusListener) OptionHelper.instantiateByClassName(listenerClass, StatusListener.class, context);
        } catch (Exception e) {
            // printing on the console is the best we can do
            e.printStackTrace();
            return null;
        }
    }

    /**
       * This utility method adds a new OnConsoleStatusListener to the context
       * passed as parameter.
       *
       * @param context
       * @since 1.0.1
       */
    static public void addOnConsoleListenerInstance(Context context, OnConsoleStatusListener onConsoleStatusListener) {
        onConsoleStatusListener.setContext(context);
        boolean effectivelyAdded = context.getStatusManager().add(onConsoleStatusListener);
        if (effectivelyAdded) {
            onConsoleStatusListener.start();
        }
    }
}
