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
import ch.qos.logback.core.util.OptionHelper;

/**
 * In case abbreviation service is requested, NamedConverter will convert fully qualified class names 
 * to their abbreviated from. NamedConverter instances will store abbreviated names in an internal LRU 
 * cache.
 * 
 * The cache will double in size if he cache miss rate is consistently above 30%. Assuming a high miss rate, 
 * the doubling until a maximum size of 2048 is attained. If at this point the cache miss rate is still 
 * too high, NamedConverter will revert to non cached behavior.

 * The general assumption here is that a large majority of logger names are concentrated within a 
 * group of approximately 1000 logger names.

 * @author Ceki Gulcu
 *
 */
public abstract class NamedConverter extends ClassicConverter {

	private static final String DISABLE_CACHE_SYSTEM_PROPERTY = "logback.namedConverter.disableCache";

	private static final int INITIAL_CACHE_SIZE = 512;
	private static final double LOAD_FACTOR = 0.75; // this is the JDK implementation default

	/**
	 * We don't let the cache map size to go over MAX_ALLOWED_REMOVAL_THRESHOLD
	 * elements
	 */
	private static final int MAX_ALLOWED_REMOVAL_THRESHOLD = (int) (2048 * LOAD_FACTOR);

	/**
	 * When the cache miss rate is above 30%, the cache is deemed inefficient.
	 */
	private static final double CACHE_MISSRATE_TRIGGER = 0.3d;

	/**
	 * We should have a sample size of minimal length before computing the cache
	 * miss rate.
	 */
	private static final int MIN_SAMPLE_SIZE = 1024;

	private static final double NEGATIVE = -1;
	private volatile boolean cacheEnabled = true;

	private final NameCache cache = new NameCache(INITIAL_CACHE_SIZE);

	private Abbreviator abbreviator = null;

	private volatile int cacheMisses = 0;
	private volatile int totalCalls = 0;

	/**
	 * Gets fully qualified name from event.
	 * 
	 * @param event The LoggingEvent to process, cannot not be null.
	 * @return name, must not be null.
	 */
	protected abstract String getFullyQualifiedName(final ILoggingEvent event);

	public void start() {

		String disableCacheProp = OptionHelper.getSystemProperty(DISABLE_CACHE_SYSTEM_PROPERTY);
		boolean disableCache = OptionHelper.toBoolean(disableCacheProp, false);
		if (disableCache) {
			addInfo("Disabling name cache via System.properties");
			this.cacheEnabled = false;
		}

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
		super.start();
	}

	public String convert(ILoggingEvent event) {
		String fqn = getFullyQualifiedName(event);

		if (abbreviator == null) {
			return fqn;
		} else {
			if (cacheEnabled) {
				return viaCache(fqn);
			} else {
				return abbreviator.abbreviate(fqn);
			}
		}
	}

	/**
	 *  This method is synchronized. It is the only place where the cache, a subclass of LinkedHashMap, 
	 *  is modified. 
	 *  
	 *  The cache can be cleared via a call to disableCache(). However, the call to disableCache() is made 
	 *  indirectly from within viaCache(String).
	 *  
	 * @param fqn
	 * @return
	 */
	private synchronized String viaCache(String fqn) {
		totalCalls++;
		String abbreviated = cache.get(fqn);
		if (abbreviated == null) {
			cacheMisses++;
			abbreviated = abbreviator.abbreviate(fqn);
			cache.put(fqn, abbreviated);
		}
		return abbreviated;
	}

	private void disableCache() {
		if (!cacheEnabled)
			return;
		this.cacheEnabled = false;
		cache.clear();	
		addInfo("Disabling cache at totalCalls=" + totalCalls);
	}
	
	public double getCacheMissRate() {
		return cache.cacheMissCalculator.getCacheMissRate();
	}

	public int getCacheMisses() {
		return cacheMisses;
	}

	private class NameCache extends LinkedHashMap<String, String> {

		private static final long serialVersionUID = 1050866539278406045L;

		int removalThreshold;
		CacheMissCalculator cacheMissCalculator = new CacheMissCalculator();

		NameCache(int initialCapacity) {
			super(initialCapacity);
			this.removalThreshold = (int) (initialCapacity * LOAD_FACTOR);
		}

		/**
		 * In the JDK tested, this method is called for every map insertion.
		 * 
		 */
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, String> entry) {
			if (shouldDoubleRemovalThreshold()) {
				removalThreshold *= 2;

				int missRate = (int) (cacheMissCalculator.getCacheMissRate() * 100);

				NamedConverter.this.addInfo("Doubling nameCache removalThreshold to " + removalThreshold
						+ " previous cacheMissRate=" + missRate + "%");
				cacheMissCalculator.updateMilestones();
			}

			if (size() >= removalThreshold) {
				return true;
			} else
				return false;
		}

		private boolean shouldDoubleRemovalThreshold() {

			double rate = cacheMissCalculator.getCacheMissRate();

			// negative rate indicates insufficient sample size
			if (rate < 0)
				return false;

			if (rate < CACHE_MISSRATE_TRIGGER)
				return false;

			// cannot double removalThreshold is already at max allowed size
			if (this.removalThreshold >= MAX_ALLOWED_REMOVAL_THRESHOLD) {
				NamedConverter.this.disableCache();
				return false;
			}

			return true;
		}
	}

	class CacheMissCalculator {

		int totalsMilestone = 0;
		int cacheMissesMilestone = 0;

		void updateMilestones() {
			this.totalsMilestone = NamedConverter.this.totalCalls;
			this.cacheMissesMilestone = NamedConverter.this.cacheMisses;
		}

		double getCacheMissRate() {

			int effectiveTotal = NamedConverter.this.totalCalls - totalsMilestone;

			if (effectiveTotal < MIN_SAMPLE_SIZE) {
				// cache miss rate cannot be negative. Woth a negative value, we signal the caller of
				// insufficient sample size.
				return NEGATIVE;
			}

			int effectiveCacheMisses = NamedConverter.this.cacheMisses - cacheMissesMilestone;
			return (1.0d * effectiveCacheMisses / effectiveTotal);
		}
	}

}
