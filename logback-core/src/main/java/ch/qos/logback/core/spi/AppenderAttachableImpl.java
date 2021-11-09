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

import java.util.Iterator;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.util.COWArrayList;

/**
 * A {@link COWArrayList} based implementation of the {@link AppenderAttachable} interface.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class AppenderAttachableImpl<E> implements AppenderAttachable<E> {

	@SuppressWarnings("unchecked")
	final private COWArrayList<Appender<E>> appenderList = new COWArrayList<>(new Appender[0]);

	/**
	 * Attach an appender. If the appender is already in the list in won't be
	 * added again.
	 */
	@Override
	public void addAppender(final Appender<E> newAppender) {
		if (newAppender == null) {
			throw new IllegalArgumentException("Null argument disallowed");
		}
		appenderList.addIfAbsent(newAppender);
	}

	/**
	 * Call the <code>doAppend</code> method on all attached appenders.
	 */
	public int appendLoopOnAppenders(final E e) {
		int size = 0;
		final Appender<E>[] appenderArray = appenderList.asTypedArray();
		final int len = appenderArray.length;
		for (int i = 0; i < len; i++) {
			appenderArray[i].doAppend(e);
			size++;
		}
		return size;
	}

	/**
	 * Get all attached appenders as an Enumeration. If there are no attached
	 * appenders <code>null</code> is returned.
	 *
	 * @return Iterator An iterator of attached appenders.
	 */
	@Override
	public Iterator<Appender<E>> iteratorForAppenders() {
		return appenderList.iterator();
	}

	/**
	 * Look for an attached appender named as <code>name</code>.
	 *
	 * <p> Return the appender with that name if in the list. Return null
	 * otherwise.
	 */
	@Override
	public Appender<E> getAppender(final String name) {
		if (name == null) {
			return null;
		}
		for (final Appender<E> appender : appenderList) {
			if (name.equals(appender.getName())) {
				return appender;
			}
		}
		return null;
	}

	/**
	 * Returns <code>true</code> if the specified appender is in the list of
	 * attached appenders, <code>false</code> otherwise.
	 *
	 * @since 1.2
	 */
	@Override
	public boolean isAttached(final Appender<E> appender) {
		if (appender == null) {
			return false;
		}
		for (final Appender<E> a : appenderList) {
			if (a == appender) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Remove and processPriorToRemoval all previously attached appenders.
	 */
	@Override
	public void detachAndStopAllAppenders() {
		for (final Appender<E> a : appenderList) {
			a.stop();
		}
		appenderList.clear();
	}

	/**
	 * Remove the appender passed as parameter form the list of attached
	 * appenders.
	 */
	@Override
	public boolean detachAppender(final Appender<E> appender) {
		if (appender == null) {
			return false;
		}
		final boolean result;
		return appenderList.remove(appender);
	}

	/**
	 * Remove the appender with the name passed as parameter form the list of
	 * appenders.
	 */
	@Override
	public boolean detachAppender(final String name) {
		if (name == null) {
			return false;
		}
		boolean removed = false;
		for (final Appender<E> a : appenderList.asTypedArray()) {
			if (name.equals(a.getName())) {
				removed = appenderList.remove(a);
				break;
			}
		}
		return removed;
	}
}
