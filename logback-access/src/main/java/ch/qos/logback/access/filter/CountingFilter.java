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
package ch.qos.logback.access.filter;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class CountingFilter extends Filter<IAccessEvent> {

	long total = 0;
	final StatisticalViewImpl accessStatsImpl;

	String domain = "ch.qos.logback.access";

	public CountingFilter() {
		accessStatsImpl = new StatisticalViewImpl(this);
	}

	@Override
	public FilterReply decide(final IAccessEvent event) {
		total++;
		accessStatsImpl.update();
		return FilterReply.NEUTRAL;
	}

	public long getTotal() {
		return total;
	}

	@Override
	public void start() {
		final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			final ObjectName on = new ObjectName(domain + ":Name=" + getName());
			final StandardMBean mbean = new StandardMBean(accessStatsImpl, StatisticalView.class);
			if (mbs.isRegistered(on)) {
				mbs.unregisterMBean(on);
			}
			mbs.registerMBean(mbean, on);
			super.start();
		} catch (final Exception e) {
			addError("Failed to create mbean", e);
		}
	}

	@Override
	public void stop() {
		super.stop();
		try {
			final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			final ObjectName on = new ObjectName("totp:Filter=1");
			mbs.unregisterMBean(on);
		} catch (final Exception e) {
			addError("Failed to unregister mbean", e);
		}
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(final String domain) {
		this.domain = domain;
	}

}
