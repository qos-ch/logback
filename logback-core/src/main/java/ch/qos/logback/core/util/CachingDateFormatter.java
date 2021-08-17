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
package ch.qos.logback.core.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A CAS implementation of DateTimeFormatter (previously SimpleDateFormat) 
 * which caches results for the duration of a millisecond.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since 0.9.29
 */
public class CachingDateFormatter {

	final DateTimeFormatter dtf;
	final ZoneId zoneId;
	final AtomicReference<CacheTuple> atomicReference;
	
	static class CacheTuple {
		final long lastTimestamp;
		final String cachedStr;
		
		public CacheTuple(long lastTimestamp, String cachedStr) {
			super();
			this.lastTimestamp = lastTimestamp;
			this.cachedStr = cachedStr;
		}
	}
	
	public CachingDateFormatter(String pattern) {
		this(pattern, null);
	}

	public CachingDateFormatter(String pattern, ZoneId aZoneId) {
		dtf = DateTimeFormatter.ofPattern(pattern);
		if(aZoneId == null) {
			this.zoneId = ZoneId.systemDefault();
		} else {
			this.zoneId = aZoneId;
			
		}
		dtf.withZone(this.zoneId);
		CacheTuple cacheTuple = new CacheTuple(-1, null);
		this.atomicReference = new AtomicReference<>(cacheTuple);
	}

	
	public final String format(long now) {
		CacheTuple localCacheTuple = atomicReference.get();
		CacheTuple oldCacheTuple = localCacheTuple;
		
		if (now != localCacheTuple.lastTimestamp) {
			Instant instant = Instant.ofEpochMilli(now);
			OffsetDateTime currentTime = OffsetDateTime.ofInstant(instant, this.zoneId);
			String result = dtf.format(currentTime);
			localCacheTuple = new CacheTuple(now, result);
			// allow a single thread to update the cache reference
			atomicReference.compareAndSet(oldCacheTuple, localCacheTuple);
		}
		return localCacheTuple.cachedStr;
	}



}
