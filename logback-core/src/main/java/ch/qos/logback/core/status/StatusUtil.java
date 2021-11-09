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
package ch.qos.logback.core.status;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;

public class StatusUtil {

	StatusManager sm;

	public StatusUtil(final StatusManager sm) {
		this.sm = sm;
	}

	public StatusUtil(final Context context) {
		sm = context.getStatusManager();
	}

	/**
	 * Returns true if the StatusManager associated with the context passed
	 * as parameter has one or more StatusListener instances registered. Returns
	 * false otherwise.
	 *
	 * @param context
	 * @return true if one or more StatusListeners registered, false otherwise
	 * @since 1.0.8
	 */
	static public boolean contextHasStatusListener(final Context context) {
		final StatusManager sm = context.getStatusManager();
		if (sm == null) {
			return false;
		}
		final List<StatusListener> listeners = sm.getCopyOfStatusListenerList();
		if (listeners == null || listeners.size() == 0) {
			return false;
		}
		return true;
	}

	static public List<Status> filterStatusListByTimeThreshold(final List<Status> rawList, final long threshold) {
		final List<Status> filteredList = new ArrayList<>();
		for (final Status s : rawList) {
			if (s.getDate() >= threshold) {
				filteredList.add(s);
			}
		}
		return filteredList;
	}

	public void addStatus(final Status status) {
		if (sm != null) {
			sm.add(status);
		}
	}

	public void addInfo(final Object caller, final String msg) {
		addStatus(new InfoStatus(msg, caller));
	}

	public void addWarn(final Object caller, final String msg) {
		addStatus(new WarnStatus(msg, caller));
	}

	public void addError(final Object caller, final String msg, final Throwable t) {
		addStatus(new ErrorStatus(msg, caller, t));
	}

	public boolean hasXMLParsingErrors(final long threshold) {
		return containsMatch(threshold, Status.ERROR, CoreConstants.XML_PARSING);
	}

	public boolean noXMLParsingErrorsOccurred(final long threshold) {
		return !hasXMLParsingErrors(threshold);
	}

	public int getHighestLevel(final long threshold) {
		final List<Status> filteredList = filterStatusListByTimeThreshold(sm.getCopyOfStatusList(), threshold);
		int maxLevel = Status.INFO;
		for (final Status s : filteredList) {
			if (s.getLevel() > maxLevel) {
				maxLevel = s.getLevel();
			}
		}
		return maxLevel;
	}

	public boolean isErrorFree(final long threshold) {
		return Status.ERROR > getHighestLevel(threshold);
	}

	public boolean isWarningOrErrorFree(final long threshold) {
		return Status.WARN > getHighestLevel(threshold);
	}

	public boolean containsMatch(final long threshold, final int level, final String regex) {
		final List<Status> filteredList = filterStatusListByTimeThreshold(sm.getCopyOfStatusList(), threshold);
		final Pattern p = Pattern.compile(regex);

		for (final Status status : filteredList) {
			if (level != status.getLevel()) {
				continue;
			}
			final String msg = status.getMessage();
			final Matcher matcher = p.matcher(msg);
			if (matcher.lookingAt()) {
				return true;
			}
		}
		return false;
	}

	public boolean containsMatch(final int level, final String regex) {
		return containsMatch(0, level, regex);
	}

	public boolean containsMatch(final String regex) {
		final Pattern p = Pattern.compile(regex);
		for (final Status status : sm.getCopyOfStatusList()) {
			final String msg = status.getMessage();
			final Matcher matcher = p.matcher(msg);
			if (matcher.lookingAt()) {
				return true;
			}
		}
		return false;
	}

	public int matchCount(final String regex) {
		int count = 0;
		final Pattern p = Pattern.compile(regex);
		for (final Status status : sm.getCopyOfStatusList()) {
			final String msg = status.getMessage();
			final Matcher matcher = p.matcher(msg);
			if (matcher.lookingAt()) {
				count++;
			}
		}
		return count;
	}

	public boolean containsException(final Class<?> exceptionType) {
		final Iterator<Status> stati = sm.getCopyOfStatusList().iterator();
		while (stati.hasNext()) {
			final Status status = stati.next();
			Throwable t = status.getThrowable();
			while (t != null) {
				if (t.getClass().getName().equals(exceptionType.getName())) {
					return true;
				}
				t = t.getCause();
			}
		}
		return false;
	}

	/**
	 * Return the time of last reset. -1 if last reset time could not be found
	 *
	 * @return time of last reset or -1
	 */
	public long timeOfLastReset() {
		final List<Status> statusList = sm.getCopyOfStatusList();
		if (statusList == null) {
			return -1;
		}

		final int len = statusList.size();
		for (int i = len - 1; i >= 0; i--) {
			final Status s = statusList.get(i);
			if (CoreConstants.RESET_MSG_PREFIX.equals(s.getMessage())) {
				return s.getDate();
			}
		}
		return -1;
	}

}
