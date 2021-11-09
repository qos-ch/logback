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

import java.net.URL;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class ConfigurationWatchListUtil {

	final static ConfigurationWatchListUtil origin = new ConfigurationWatchListUtil();

	private ConfigurationWatchListUtil() {
	}


	public static void registerConfigurationWatchList(final Context context, final ConfigurationWatchList cwl) {
		context.putObject(CoreConstants.CONFIGURATION_WATCH_LIST, cwl);
	}
	public static void setMainWatchURL(final Context context, final URL url) {
		ConfigurationWatchList cwl = getConfigurationWatchList(context);
		if (cwl == null) {
			cwl = new ConfigurationWatchList();
			cwl.setContext(context);
			context.putObject(CoreConstants.CONFIGURATION_WATCH_LIST, cwl);
		} else {
			cwl.clear();
		}
		//setConfigurationWatchListResetFlag(context, true);
		cwl.setMainURL(url);
	}

	public static URL getMainWatchURL(final Context context) {
		final ConfigurationWatchList cwl = getConfigurationWatchList(context);
		if (cwl == null) {
			return null;
		}
		return cwl.getMainURL();
	}

	public static void addToWatchList(final Context context, final URL url) {
		final ConfigurationWatchList cwl = getConfigurationWatchList(context);
		if (cwl == null) {
			addWarn(context, "Null ConfigurationWatchList. Cannot add " + url);
		} else {
			addInfo(context, "Adding [" + url + "] to configuration watch list.");
			cwl.addToWatchList(url);
		}
	}

	//    public static boolean wasConfigurationWatchListReset(Context context) {
	//        Object o = context.getObject(CoreConstants.CONFIGURATION_WATCH_LIST_RESET);
	//        if (o == null)
	//            return false;
	//        else {
	//            return ((Boolean) o).booleanValue();
	//        }
	//    }

	//    public static void setConfigurationWatchListResetFlag(Context context, boolean val) {
	//        context.putObject(CoreConstants.CONFIGURATION_WATCH_LIST_RESET, new Boolean(val));
	//    }

	public static ConfigurationWatchList getConfigurationWatchList(final Context context) {
		return (ConfigurationWatchList) context.getObject(CoreConstants.CONFIGURATION_WATCH_LIST);
	}

	static void addStatus(final Context context, final Status s) {
		if (context == null) {
			System.out.println("Null context in " + ConfigurationWatchList.class.getName());
			return;
		}
		final StatusManager sm = context.getStatusManager();
		if (sm == null) {
			return;
		}
		sm.add(s);
	}

	static void addInfo(final Context context, final String msg) {
		addStatus(context, new InfoStatus(msg, origin));
	}

	static void addWarn(final Context context, final String msg) {
		addStatus(context, new WarnStatus(msg, origin));
	}
}
