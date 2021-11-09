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
package ch.qos.logback.classic.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import ch.qos.logback.core.CoreConstants;

public class ThrowableProxy implements IThrowableProxy {

	static final StackTraceElementProxy[] EMPTY_STEP = {};

	private final Throwable throwable;
	private final String className;
	private final String message;
	// package-private because of ThrowableProxyUtil
	StackTraceElementProxy[] stackTraceElementProxyArray;
	// package-private because of ThrowableProxyUtil
	int commonFrames;
	private ThrowableProxy cause;
	private ThrowableProxy[] suppressed = NO_SUPPRESSED;

	// private final Set<Throwable> alreadyProcessedSet;

	private transient PackagingDataCalculator packagingDataCalculator;
	private boolean calculatedPackageData = false;

	private final boolean circular;

	private static final ThrowableProxy[] NO_SUPPRESSED = {};

	public ThrowableProxy(final Throwable throwable) {
		// use an identity set to detect cycles in the throwable chain
		this(throwable, Collections.newSetFromMap(new IdentityHashMap<>()));
	}

	// used for circular exceptions
	private ThrowableProxy(final Throwable circular, final boolean isCircular) {
		throwable = circular;
		className = circular.getClass().getName();
		message = circular.getMessage();
		stackTraceElementProxyArray = EMPTY_STEP;
		this.circular = true;
	}

	public ThrowableProxy(final Throwable throwable, final Set<Throwable> alreadyProcessedSet) {

		this.throwable = throwable;
		className = throwable.getClass().getName();
		message = throwable.getMessage();
		stackTraceElementProxyArray = ThrowableProxyUtil.steArrayToStepArray(throwable.getStackTrace());
		circular = false;

		alreadyProcessedSet.add(throwable);

		final Throwable nested = throwable.getCause();
		if (nested != null) {
			if (alreadyProcessedSet.contains(nested)) {
				cause = new ThrowableProxy(nested, true);
			} else {
				cause = new ThrowableProxy(nested, alreadyProcessedSet);
				cause.commonFrames = ThrowableProxyUtil.findNumberOfCommonFrames(nested.getStackTrace(),
						stackTraceElementProxyArray);
			}
		}

		final Throwable[] throwableSuppressed = throwable.getSuppressed();
		if (throwableSuppressed.length > 0) {
			final List<ThrowableProxy> suppressedList = new ArrayList<>(throwableSuppressed.length);
			for (final Throwable sup : throwableSuppressed) {
				if (alreadyProcessedSet.contains(sup)) {
					final ThrowableProxy throwableProxy = new ThrowableProxy(sup, true);
					suppressedList.add(throwableProxy);
				} else {
					final ThrowableProxy throwableProxy = new ThrowableProxy(sup, alreadyProcessedSet);
					throwableProxy.commonFrames = ThrowableProxyUtil.findNumberOfCommonFrames(sup.getStackTrace(),
							stackTraceElementProxyArray);
					suppressedList.add(throwableProxy);
				}
			}
			suppressed = suppressedList.toArray(new ThrowableProxy[suppressedList.size()]);
		}
	}

	public Throwable getThrowable() {
		return throwable;
	}

	@Override
	public String getMessage() {
		return message;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ch.qos.logback.classic.spi.IThrowableProxy#getClassName()
	 */
	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public StackTraceElementProxy[] getStackTraceElementProxyArray() {
		return stackTraceElementProxyArray;
	}


	@Override
	public boolean isCyclic() {
		return circular;
	}

	@Override
	public int getCommonFrames() {
		return commonFrames;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ch.qos.logback.classic.spi.IThrowableProxy#getCause()
	 */
	@Override
	public IThrowableProxy getCause() {
		return cause;
	}

	@Override
	public IThrowableProxy[] getSuppressed() {
		return suppressed;
	}

	public PackagingDataCalculator getPackagingDataCalculator() {
		// if original instance (non-deserialized), and packagingDataCalculator
		// is not already initialized, then create an instance.
		// here we assume that (throwable == null) for deserialized instances
		if (throwable != null && packagingDataCalculator == null) {
			packagingDataCalculator = new PackagingDataCalculator();
		}
		return packagingDataCalculator;
	}

	public void calculatePackagingData() {
		if (calculatedPackageData) {
			return;
		}
		final PackagingDataCalculator pdc = getPackagingDataCalculator();
		if (pdc != null) {
			calculatedPackageData = true;
			pdc.calculate(this);
		}
	}

	public void fullDump() {
		final StringBuilder builder = new StringBuilder();
		for (final StackTraceElementProxy step : stackTraceElementProxyArray) {
			final String string = step.toString();
			builder.append(CoreConstants.TAB).append(string);
			ThrowableProxyUtil.subjoinPackagingData(builder, step);
			builder.append(CoreConstants.LINE_SEPARATOR);
		}
		System.out.println(builder.toString());
	}

}
