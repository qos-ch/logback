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

import java.io.PrintStream;
import java.util.List;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.StatusPrinter;

/**
 *  Print all new incoming status messages on the on the designated PrintStream.
 * @author Ceki G&uuml;lc&uuml;
 */
abstract public class OnPrintStreamStatusListenerBase extends ContextAwareBase implements StatusListener, LifeCycle {

	boolean isStarted = false;

	static final long DEFAULT_RETROSPECTIVE = 300;
	long retrospectiveThresold = DEFAULT_RETROSPECTIVE;

	/**
	 * The prefix to place before each status message
	 * @since 1.1.10
	 */
	String prefix;

	/**
	 * The PrintStream used by derived classes
	 * @return
	 */
	abstract protected PrintStream getPrintStream();

	private void print(final Status status) {
		final StringBuilder sb = new StringBuilder();
		if(prefix != null) {
			sb.append(prefix);
		}

		StatusPrinter.buildStr(sb, "", status);
		getPrintStream().print(sb);
	}

	@Override
	public void addStatusEvent(final Status status) {
		if (!isStarted) {
			return;
		}
		print(status);
	}

	/**
	 * Print status messages retrospectively
	 */
	private void retrospectivePrint() {
		if (context == null) {
			return;
		}
		final long now = System.currentTimeMillis();
		final StatusManager sm = context.getStatusManager();
		final List<Status> statusList = sm.getCopyOfStatusList();
		for (final Status status : statusList) {
			final long timestampOfStatusMesage = status.getDate();
			if (isElapsedTimeLongerThanThreshold(now, timestampOfStatusMesage)) {
				print(status);
			}
		}
	}

	private boolean isElapsedTimeLongerThanThreshold(final long now, final long timestamp) {
		final long elapsedTime = now - timestamp;
		return elapsedTime < retrospectiveThresold;
	}

	/**
	 * Invoking the start method can cause the instance to print status messages created less than
	 * value of retrospectiveThresold.
	 */
	@Override
	public void start() {
		isStarted = true;
		if (retrospectiveThresold > 0) {
			retrospectivePrint();
		}
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(final String prefix) {
		this.prefix = prefix;
	}

	public void setRetrospective(final long retrospective) {
		retrospectiveThresold = retrospective;
	}

	public long getRetrospective() {
		return retrospectiveThresold;
	}

	@Override
	public void stop() {
		isStarted = false;
	}

	@Override
	public boolean isStarted() {
		return isStarted;
	}

}
