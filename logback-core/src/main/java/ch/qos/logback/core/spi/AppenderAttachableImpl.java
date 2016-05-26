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
import java.util.concurrent.CopyOnWriteArrayList;

import ch.qos.logback.core.Appender;

/**
 * A ReentrantReadWriteLock based implementation of the
 * {@link AppenderAttachable} interface.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class AppenderAttachableImpl<E> implements AppenderAttachable<E> {

    final private CopyOnWriteArrayList<Appender<E>> appenderList = new CopyOnWriteArrayList<Appender<E>>();

    /**
     * Attach an appender. If the appender is already in the list in won't be
     * added again.
     */
    public void addAppender(Appender<E> newAppender) {
        if (newAppender == null) {
            throw new IllegalArgumentException("Null argument disallowed");
        }
        appenderList.addIfAbsent(newAppender);
    }

    /**
     * Call the <code>doAppend</code> method on all attached appenders.
     */
    public int appendLoopOnAppenders(E e) {
        int size = 0;
        for (Appender<E> appender : appenderList) {
            appender.doAppend(e);
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
    public Iterator<Appender<E>> iteratorForAppenders() {
        return appenderList.iterator();
    }

    /**
     * Look for an attached appender named as <code>name</code>.
     * <p/>
     * <p> Return the appender with that name if in the list. Return null
     * otherwise.
     */
    public Appender<E> getAppender(String name) {
        if (name == null) {
            return null;
        }
        for (Appender<E> appender : appenderList) {
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
    public boolean isAttached(Appender<E> appender) {
        if (appender == null) {
            return false;
        }
        for (Appender<E> a : appenderList) {
            if (a == appender)
                return true;
        }
        return false;
    }

    /**
     * Remove and processPriorToRemoval all previously attached appenders.
     */
    public void detachAndStopAllAppenders() {
        for (Appender<E> a : appenderList) {
            a.stop();
        }
        appenderList.clear();
    }

    static final long START = System.currentTimeMillis();

    /**
     * Remove the appender passed as parameter form the list of attached
     * appenders.
     */
    public boolean detachAppender(Appender<E> appender) {
        if (appender == null) {
            return false;
        }
        boolean result;
        result = appenderList.remove(appender);
        return result;
    }

    /**
     * Remove the appender with the name passed as parameter form the list of
     * appenders.
     */
    public boolean detachAppender(String name) {
        if (name == null) {
            return false;
        }
        boolean removed = false;
        for (Appender<E> a : appenderList) {
            if (name.equals((a).getName())) {
                removed = appenderList.remove(a);
                break;
            }
        }
        return removed;
    }
}
