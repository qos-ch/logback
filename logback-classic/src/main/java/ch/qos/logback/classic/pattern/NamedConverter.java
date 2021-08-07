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
package ch.qos.logback.classic.pattern;

import java.util.LinkedHashMap;
import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;

public abstract class NamedConverter extends ClassicConverter {
	static final int INITIAL_CACHE_SIZE = 128;
	static final int MAX_ALLOWED_REMOVAL_THRESHOLD = 2048;
	static final double CACHE_MISSRATE_TRIGGER = 0.3d;
	static final int MIN_SAMPLE_SIZE = MAX_ALLOWED_REMOVAL_THRESHOLD * 8;

	final NameCache cache = new NameCache(INITIAL_CACHE_SIZE);

	Abbreviator abbreviator = null;

	volatile int cacheMisses = 0;
	volatile int totalCalls = 0;

	/**
	 * Gets fully qualified name from event.
	 * 
	 * @param event The LoggingEvent to process, cannot not be null.
	 * @return name, must not be null.
	 */
	protected abstract String getFullyQualifiedName(final ILoggingEvent event);

	public void start() {
		String optStr = getFirstOption();
		if (optStr != null) {
			try {
				int targetLen = Integer.parseInt(optStr);
				if (targetLen == 0) {
					abbreviator = new ClassNameOnlyAbbreviator();
				} else if (targetLen > 0) {
					abbreviator = new TargetLengthBasedClassNameAbbreviator(targetLen);
				}
			} catch (NumberFormatException nfe) {
				addError("failed to parse integer string [" + optStr + "]", nfe);
			}
		}
	}

	public String convert(ILoggingEvent event) {
		String fqn = getFullyQualifiedName(event);

		if (abbreviator == null) {
			return fqn;
		} else {
			return viaCache(fqn);
		}
	}

	private String viaCache(String fqn) {
		totalCalls++;
		String abbreviated = cache.get(fqn);
		if (abbreviated == null) {
			cacheMisses++;
			abbreviated = abbreviator.abbreviate(fqn);
			cache.put(fqn, abbreviated);
		}
		return abbreviated;
	}

	public double getCacheMissRate() {
		return cache.cacheMissCalculator.getCacheMissRate();
	}

	class NameCache extends LinkedHashMap<String, String> {

		private static final long serialVersionUID = 1050866539278406045L;

		int removalThreshold;
		CacheMissCalculator cacheMissCalculator = new CacheMissCalculator();

		NameCache(int initialCapacity) {
			super(initialCapacity);
			this.removalThreshold = (int) (0.75 * initialCapacity);
		}

		/**
		 * In the JDK tested, this method is called for every map insertion.
		 * 
		 */
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, String> entry) {
			if (shouldDoubleRemovalThreshold()) {
				NamedConverter.this.addInfo("Will double removalThreshold at " + removalThreshold + " cacheMissRate="
						+ cacheMissCalculator.getCacheMissRate());
				cacheMissCalculator.updateMilestones();
				removalThreshold *= 2;
			}

			// System.out.println("size="+size()+" removalThreshold="+removalThreshold);
			if (size() >= removalThreshold) {
				return true;
			} else
				return false;
		}

		private boolean shouldDoubleRemovalThreshold() {

			if (this.removalThreshold >= MAX_ALLOWED_REMOVAL_THRESHOLD)
				return false;

			double rate = cacheMissCalculator.getCacheMissRate();
			// System.out.println("computed getCacheMissRate="+rate);

			if (rate < 0)
				return false;

			if (rate < CACHE_MISSRATE_TRIGGER)
				return false;

			System.out.println(
					"xxxxxxxxxxxx shouldDoubleRemovalThreshold returns true at " + (int) (removalThreshold / 0.75));
			System.out.println("xxxxxxxxxxxx computed cache miss rate: " + rate);
			return true;
		}
	}

	class CacheMissCalculator {

		int totalsMilestone = 0;
		int cacheMissesMilestone = 0;

		void updateMilestones() {
			System.out.println("updateMilestones totalsMilestone=" + totalsMilestone + " cacheMissesMilestone="
					+ cacheMissesMilestone);
			this.totalsMilestone = NamedConverter.this.totalCalls;
			this.cacheMissesMilestone = NamedConverter.this.cacheMisses;
		}

		double getCacheMissRate() {

			int effectiveTotal = NamedConverter.this.totalCalls - totalsMilestone;

			if (effectiveTotal < MIN_SAMPLE_SIZE) {
				return -1;
			}

			int effectiveCacheMisses = NamedConverter.this.cacheMisses - cacheMissesMilestone;
			// System.out.println("effectiveCacheMisses = "+effectiveCacheMisses);
			return (1.0d * effectiveCacheMisses / effectiveTotal);
		}
	}

}
