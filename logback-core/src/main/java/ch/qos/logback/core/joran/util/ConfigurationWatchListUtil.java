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
package ch.qos.logback.core.joran.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;

import java.net.URL;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class ConfigurationWatchListUtil {

    final static ConfigurationWatchListUtil ORIGIN = new ConfigurationWatchListUtil();

    private ConfigurationWatchListUtil() {
    }

    public static void registerConfigurationWatchList(Context context, ConfigurationWatchList cwl) {
        context.putObject(CoreConstants.CONFIGURATION_WATCH_LIST, cwl);
    }

    public static void setMainWatchURL(Context context, URL url) {
        ConfigurationWatchList cwl = getConfigurationWatchList(context);
        if (cwl == null) {
            cwl = registerNewConfigurationWatchListWithContext(context);
        } else {
            cwl.clear();
        }
        // setConfigurationWatchListResetFlag(context, true);
        cwl.setMainURL(url);
    }

    /**
     * Returns true if there are watchable files, false otherwise.
     * @return true if there are watchable files,  false otherwise.
     * @since 1.5.8
     */
    public static boolean watchPredicateFulfilled(Context context) {
        ConfigurationWatchList cwl = getConfigurationWatchList(context);
        if (cwl == null) {
            return false;
        }
        return cwl.watchPredicateFulfilled();
    }

    public static URL getMainWatchURL(Context context) {
        ConfigurationWatchList cwl = getConfigurationWatchList(context);
        if (cwl == null) {
            return null;
        } else {
            return cwl.getMainURL();
        }
    }

    public static void addToWatchList(Context context, URL url) {
        addToWatchList(context, url, false);
    }

    public static void addToWatchList(Context context, URL url, boolean createCWL) {
        ConfigurationWatchList cwl = getConfigurationWatchList(context);
        if(cwl == null) {
            if(createCWL && isWatchable(url)) {
                cwl = registerNewConfigurationWatchListWithContext(context);
            } else {
                addWarn(context, "Null ConfigurationWatchList. Cannot add " + url);
                return;
            }
        }

        addInfo(context, "Adding [" + url + "] to configuration watch list.");
        cwl.addToWatchList(url);

    }

    private static ConfigurationWatchList registerNewConfigurationWatchListWithContext(Context context) {
        ConfigurationWatchList cwl = new ConfigurationWatchList();
        cwl.setContext(context);
        context.putObject(CoreConstants.CONFIGURATION_WATCH_LIST, cwl);
        return cwl;
    }

    private static boolean isWatchable(URL url) {
        if(url == null) {
            return false;
        }

        String protocol = url.getProtocol();
        return  "file".equalsIgnoreCase(protocol);
    }



    public static ConfigurationWatchList getConfigurationWatchList(Context context) {
        return (ConfigurationWatchList) context.getObject(CoreConstants.CONFIGURATION_WATCH_LIST);
    }

    static void addStatus(Context context, Status s) {
        if (context == null) {
            System.out.println("Null context in " + ConfigurationWatchList.class.getName());
            return;
        }
        StatusManager sm = context.getStatusManager();
        if (sm == null)
            return;
        sm.add(s);
    }

    static void addInfo(Context context, String msg) {
       addStatus(context, new InfoStatus(msg, ORIGIN));
    }

     static void addWarn(Context context, String msg) {
        addStatus(context, new WarnStatus(msg, ORIGIN));
    }
}
