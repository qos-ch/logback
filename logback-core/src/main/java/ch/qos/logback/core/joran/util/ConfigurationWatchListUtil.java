/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
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
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;

import java.net.URL;

/**
 * A thin layer on top of {@link ConfigurationWatchList}.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class ConfigurationWatchListUtil {

    final static ConfigurationWatchListUtil ORIGIN = new ConfigurationWatchListUtil();

    private ConfigurationWatchListUtil() {
    }

    public static void registerConfigurationWatchList(Context context, ConfigurationWatchList cwl) {
        context.putObject(CoreConstants.CONFIGURATION_WATCH_LIST, cwl);
    }

    /**
     * Sets the main configuration watch URL in the given context's configuration watch list.
     * If the provided URL is null, the method exits without making any changes.
     * If the configuration watch list is not initialized, an error is added to the context's status.
     * Otherwise, the configuration watch list is cleared and the given URL is set as the main URL.
     *
     * @param context the context in which the configuration watch list is managed
     * @param url the main configuration watch URL to be set; if null, no action is taken
     */
    public static void setMainWatchURL(Context context, URL url) {
        if(url == null) {
            return;
        }
        ConfigurationWatchList cwl = getConfigurationWatchList(context);
        if (cwl == null) {
            addError(context, "ConfigurationWatchList should have been initialized at this stage.");
            return;
        } else {
            cwl.clear();
        }
        cwl.setTopURL(url);
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
            return cwl.getTopURL();
        }
    }

    public static void addToWatchList(Context context, URL url) {
        addToWatchList(context, url, false);
    }

    public static void addToWatchList(Context context, URL url, boolean createCWL) {
        ConfigurationWatchList cwl = getConfigurationWatchList(context);
        if(cwl == null) {
            if(createCWL && ConfigurationWatchList.isWatchableProtocol(url)) {
                cwl = registerNewConfigurationWatchListWithContext(context);
            } else {
                addInfo(context, "ConfigurationWatchList not initialized due to absence of scan directive. Will not add " + url);
                return;
            }
        }

        String protocol = url.getProtocol();
        if(cwl.isWatchableProtocol(protocol)) {
            addInfo(context, "Will add [" + url + "] to configuration watch list.");
            cwl.addToWatchList(url);
        } else {
            addInfo(context, "Will not add configuration file ["+url + "] to watch list, because '"+protocol+"' protocol is not watchable.");
            addInfo(context, "Only the protocols 'file', 'http' and 'https' are watchable.");
        }
    }

    public static ConfigurationWatchList registerNewConfigurationWatchListWithContext(Context context) {
        ConfigurationWatchList cwl = new ConfigurationWatchList();
        cwl.setContext(context);
        context.putObject(CoreConstants.CONFIGURATION_WATCH_LIST, cwl);
        return cwl;
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
    static void addError(Context context, String msg) {
        addStatus(context, new ErrorStatus(msg, ORIGIN));
    }
}
