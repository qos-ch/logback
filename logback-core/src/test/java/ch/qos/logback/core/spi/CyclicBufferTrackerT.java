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
package ch.qos.logback.core.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.helpers.CyclicBuffer;

/**
 * Another tracker implementtion for testing purposes only.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class CyclicBufferTrackerT<E> implements ComponentTracker<CyclicBuffer<E>> {

	int bufferSize = CyclicBufferTracker.DEFAULT_BUFFER_SIZE;
	int maxComponents = CyclicBufferTracker.DEFAULT_NUMBER_OF_BUFFERS;

	List<TEntry<E>> liveList = new LinkedList<>();
	List<TEntry<E>> lingererList = new LinkedList<>();

	long lastCheck = 0;

	private TEntry<E> getEntry(final List<TEntry<E>> list, final String k) {
		for (final CyclicBufferTrackerT<E>.TEntry<E> te : list) {
			if (te.key.equals(k)) {
				return te;
			}
		}
		return null;
	}

	private TEntry<E> getFromEitherList(final String key) {
		final TEntry<E> entry = getEntry(liveList, key);
		if (entry != null) {
			return entry;
		}
		return getEntry(lingererList, key);
	}

	private List<String> keysAsOrderedList(final List<TEntry<E>> list) {
		Collections.sort(list);
		final List<String> result = new LinkedList<>();
		for (int i = 0; i < list.size(); i++) {
			final TEntry<E> te = list.get(i);
			result.add(te.key);
		}
		return result;
	}

	List<String> liveKeysAsOrderedList() {
		return keysAsOrderedList(liveList);
	}

	List<String> lingererKeysAsOrderedList() {
		return keysAsOrderedList(lingererList);
	}

	@Override
	public Set<String> allKeys() {
		final HashSet<String> allKeys = new HashSet<>();
		for (final TEntry<E> e : liveList) {
			allKeys.add(e.key);
		}
		for (final TEntry<E> e : lingererList) {
			allKeys.add(e.key);
		}
		return allKeys;
	}

	@Override
	public Collection<CyclicBuffer<E>> allComponents() {
		final List<CyclicBuffer<E>> allComponents = new ArrayList<>();
		for (final TEntry<E> e : liveList) {
			allComponents.add(e.value);
		}
		for (final TEntry<E> e : lingererList) {
			allComponents.add(e.value);
		}

		return allComponents;
	}

	@Override
	public CyclicBuffer<E> find(final String key) {
		final TEntry<E> te = getFromEitherList(key);
		if (te == null) {
			return null;
		}
		return te.value;
	}

	@Override
	public CyclicBuffer<E> getOrCreate(final String key, final long timestamp) {
		TEntry<E> te = getFromEitherList(key);
		if (te == null) {
			final CyclicBuffer<E> cb = new CyclicBuffer<>(bufferSize);
			te = new TEntry<>(key, cb, timestamp);
			liveList.add(te);
			if (liveList.size() > maxComponents) {
				Collections.sort(liveList);
				liveList.remove(0);
			}
		} else {
			te.timestamp = timestamp;
			Collections.sort(liveList);
		}
		return te.value;
	}

	@Override
	public void endOfLife(final String k) {
		TEntry<E> te = null;
		boolean found = false;
		for (int i = 0; i < liveList.size(); i++) {
			te = liveList.get(i);
			if (te.key.equals(k)) {
				liveList.remove(i);
				found = true;
				break;
			}
		}
		if (found) {
			lingererList.add(te);
		}
	}

	private boolean isEntryStale(final TEntry<E> entry, final long now) {
		return entry.timestamp + DEFAULT_TIMEOUT < now;
	}

	private boolean isEntryDoneLingering(final TEntry<E> tEntry, final long now) {
		return tEntry.timestamp + AbstractComponentTracker.LINGERING_TIMEOUT < now;
	}

	@Override
	public void removeStaleComponents(final long now) {
		if (isTooSoonForRemovalIteration(now)) {
			return;
		}
		// both list should be sorted before removal attempts
		Collections.sort(liveList);
		Collections.sort(lingererList);
		removeComponentsInExcessFromMainList();
		removeStaleComponentsFromMainList(now);
		removeStaleComponentsFromLingerersList(now);
	}

	private void removeComponentsInExcessFromMainList() {
		while (liveList.size() > maxComponents) {
			liveList.remove(0);
		}
	}

	private void removeStaleComponentsFromMainList(final long now) {
		while (liveList.size() != 0 && isEntryStale(liveList.get(0), now)) {
			liveList.remove(0);
		}
	}

	private void removeStaleComponentsFromLingerersList(final long now) {
		while (lingererList.size() != 0 && isEntryDoneLingering(lingererList.get(0), now)) {
			lingererList.remove(0);
		}
	}

	private boolean isTooSoonForRemovalIteration(final long now) {
		if (lastCheck + CoreConstants.MILLIS_IN_ONE_SECOND > now) {
			return true;
		}
		lastCheck = now;
		return false;
	}

	@Override
	public int getComponentCount() {
		return liveList.size() + lingererList.size();
	}

	// ==================================================================

	private class TEntry<X> implements Comparable<TEntry<?>> {

		String key;
		CyclicBuffer<E> value;
		long timestamp;

		TEntry(final String k, final CyclicBuffer<E> v, final long timestamp) {
			this.key = k;
			this.value = v;
			this.timestamp = timestamp;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			return prime * result + (key == null ? 0 : key.hashCode());
		}

		@Override
		public int compareTo(final TEntry<?> o) {
			if (!(o instanceof TEntry)) {
				throw new IllegalArgumentException("arguments must be of type " + TEntry.class);
			}

			final TEntry<?> other = o;
			if (timestamp > other.timestamp) {
				return 1;
			}
			if (timestamp == other.timestamp) {
				return 0;
			}
			return -1;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if ((obj == null) || (getClass() != obj.getClass())) {
				return false;
			}
			@SuppressWarnings("unchecked")
			final TEntry<?> other = (TEntry<?>) obj;
			if (!Objects.equals(key, other.key)) {
				return false;
			}
			if (!Objects.equals(value, other.value)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "(" + key + ", " + value + ")";
		}
	}
}
