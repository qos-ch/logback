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
package ch.qos.logback.core;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;

/**
 * This appender and derived classes, log events asynchronously. In order to
 * avoid loss of logging events, this appender should be closed. It is the
 * user's responsibility to close appenders, typically at the end of the
 * application lifecycle.
 * <p/>
 * This appender buffers events in a {@link BlockingQueue}. {@link Worker}
 * thread created by this appender takes events from the head of the queue, and
 * dispatches them to the single appender attached to this appender.
 * <p/>
 * <p>
 * Please refer to the <a
 * href="http://logback.qos.ch/manual/appenders.html#AsyncAppender">logback
 * manual</a> for further information about this appender.
 * </p>
 *
 * @param <E>
 * @author Ceki G&uuml;lc&uuml;
 * @author Torsten Juergeleit
 * @since 1.0.4
 */
public class AsyncAppenderBase<E> extends UnsynchronizedAppenderBase<E>
		implements AppenderAttachable<E> {

	protected AppenderAttachableImpl<E> aai = new AppenderAttachableImpl<E>();

	/**
	 * The default buffer size.
	 */
	public static final int DEFAULT_QUEUE_SIZE = 256;
	protected int queueSize = DEFAULT_QUEUE_SIZE;

	protected int appenderCount = 0;
	protected static final int UNDEFINED = -1;
	protected int discardingThreshold = UNDEFINED;

	// static final int UNDEFINED = -1;
	// int discardingThreshold = UNDEFINED;

	/**
	 * Is the eventObject passed as parameter discardable? The base class's
	 * implementation of this method always returns 'false' but sub-classes may
	 * (and do) override this method.
	 * <p/>
	 * <p>
	 * Note that only if the buffer is nearly full are events discarded.
	 * Otherwise, when the buffer is "not full" all events are logged.
	 *
	 * @param eventObject
	 * @return - true if the event can be discarded, false otherwise
	 */
	protected boolean isDiscardable(E eventObject) {
		return false;
	}

	@Override
	protected void append(E eventObject) {
		if (isQueueBelowDiscardingThreshold() && isDiscardable(eventObject)) {
			return;
		}
		preprocess(eventObject);
		put(eventObject);
	}

	protected boolean isQueueBelowDiscardingThreshold() {
		return false;
	}

	/**
	 * Pre-process the event prior to queueing. The base class does no
	 * pre-processing but sub-classes can override this behavior.
	 *
	 * @param eventObject
	 */
	protected void preprocess(E eventObject) {
	}

	protected void put(E eventObject) {
	}

	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}

	public int getDiscardingThreshold() {
		return discardingThreshold;
	}

	public void setDiscardingThreshold(int discardingThreshold) {
		this.discardingThreshold = discardingThreshold;
	}

	public void addAppender(Appender<E> newAppender) {
		appenderCount++;
		addInfo("Attaching appender named [" + newAppender.getName()
				+ "] to AsyncAppender.");
		aai.addAppender(newAppender);
	}

	public Iterator<Appender<E>> iteratorForAppenders() {
		return aai.iteratorForAppenders();
	}

	public Appender<E> getAppender(String name) {
		return aai.getAppender(name);
	}

	public boolean isAttached(Appender<E> eAppender) {
		return aai.isAttached(eAppender);
	}

	public void detachAndStopAllAppenders() {
		aai.detachAndStopAllAppenders();
	}

	public boolean detachAppender(Appender<E> eAppender) {
		return aai.detachAppender(eAppender);
	}

	public boolean detachAppender(String name) {
		return aai.detachAppender(name);
	}
}
