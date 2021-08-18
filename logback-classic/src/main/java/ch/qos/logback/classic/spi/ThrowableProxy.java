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

import ch.qos.logback.core.CoreConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class ThrowableProxy implements IThrowableProxy {

	static final StackTraceElementProxy[] EMPTY_STEP = new StackTraceElementProxy[0]; 
			
	private Throwable throwable;
	private String className;
	private String message;
	// package-private because of ThrowableProxyUtil
	StackTraceElementProxy[] stackTraceElementProxyArray;
	// package-private because of ThrowableProxyUtil
	int commonFrames;
	private ThrowableProxy cause;
	private ThrowableProxy[] suppressed = NO_SUPPRESSED;

	// private final Set<Throwable> alreadyProcessedSet;

	private transient PackagingDataCalculator packagingDataCalculator;
	private boolean calculatedPackageData = false;

	private boolean circular;

	private static final ThrowableProxy[] NO_SUPPRESSED = new ThrowableProxy[0];

	public ThrowableProxy(Throwable throwable) {
		// use an identity set to detect cycles in the throwable chain
		this(throwable, Collections.newSetFromMap(new IdentityHashMap<>()));
	}

	// used for circular exceptions
	private ThrowableProxy(Throwable circular, boolean isCircular) {
		this.throwable = circular;
		this.className = circular.getClass().getName();
		this.message = circular.getMessage();
		this.stackTraceElementProxyArray = EMPTY_STEP;
		this.circular = true;
	}

	public ThrowableProxy(Throwable throwable, Set<Throwable> alreadyProcessedSet) {

		this.throwable = throwable;
		this.className = throwable.getClass().getName();
		this.message = throwable.getMessage();
		this.stackTraceElementProxyArray = ThrowableProxyUtil.steArrayToStepArray(throwable.getStackTrace());
		this.circular = false;
		
		alreadyProcessedSet.add(throwable);

		Throwable nested = throwable.getCause();
		if (nested != null) {
			if (alreadyProcessedSet.contains(nested)) {
				this.cause = new ThrowableProxy(nested, true);
			} else {
				this.cause = new ThrowableProxy(nested, alreadyProcessedSet);
				this.cause.commonFrames = ThrowableProxyUtil.findNumberOfCommonFrames(nested.getStackTrace(),
						stackTraceElementProxyArray);
			}
		}

		Throwable[] throwableSuppressed = throwable.getSuppressed();
		if (throwableSuppressed.length > 0) {
			List<ThrowableProxy> suppressedList = new ArrayList<>(throwableSuppressed.length);
			for (Throwable sup : throwableSuppressed) {
				if (alreadyProcessedSet.contains(sup)) {
					ThrowableProxy throwableProxy = new ThrowableProxy(sup, true);
					suppressedList.add(throwableProxy);
				} else {
					ThrowableProxy throwableProxy = new ThrowableProxy(sup, alreadyProcessedSet);
					throwableProxy.commonFrames = ThrowableProxyUtil.findNumberOfCommonFrames(sup.getStackTrace(),
							stackTraceElementProxyArray);
					suppressedList.add(throwableProxy);
				}
			}
			this.suppressed = suppressedList.toArray(new ThrowableProxy[suppressedList.size()]);
		}
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public String getMessage() {
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.qos.logback.classic.spi.IThrowableProxy#getClassName()
	 */
	public String getClassName() {
		return className;
	}

	public StackTraceElementProxy[] getStackTraceElementProxyArray() {
		return stackTraceElementProxyArray;
	}


	@Override
	public boolean isCyclic() {
		return circular;
	}
	
	public int getCommonFrames() {
		return commonFrames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.qos.logback.classic.spi.IThrowableProxy#getCause()
	 */
	public IThrowableProxy getCause() {
		return cause;
	}

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
		PackagingDataCalculator pdc = this.getPackagingDataCalculator();
		if (pdc != null) {
			calculatedPackageData = true;
			pdc.calculate(this);
		}
	}

	public void fullDump() {
		StringBuilder builder = new StringBuilder();
		for (StackTraceElementProxy step : stackTraceElementProxyArray) {
			String string = step.toString();
			builder.append(CoreConstants.TAB).append(string);
			ThrowableProxyUtil.subjoinPackagingData(builder, step);
			builder.append(CoreConstants.LINE_SEPARATOR);
		}
		System.out.println(builder.toString());
	}

}
