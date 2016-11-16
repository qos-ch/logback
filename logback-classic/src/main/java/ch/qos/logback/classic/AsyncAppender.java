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
package ch.qos.logback.classic;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ch.qos.logback.classic.disruptor.LogDisruptorEventHandle;
import ch.qos.logback.classic.disruptor.LogProducerTranslator;
import ch.qos.logback.classic.disruptor.LogValueEvent;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AsyncAppenderBase;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

/**
 * In order to optimize performance this appender deems events of level TRACE,
 * DEBUG and INFO as discardable. See the <a
 * href="http://logback.qos.ch/manual/appenders.html#AsyncAppender">chapter on
 * appenders</a> in the manual for further information.
 *
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.0.4
 */
public class AsyncAppender extends AsyncAppenderBase<ILoggingEvent> {

	private boolean includeCallerData = false;
	private Disruptor<LogValueEvent> disruptor;
	private RingBuffer<LogValueEvent> ringBuffer;

	@SuppressWarnings("unchecked")
	@Override
	public void start() {
		if (appenderCount == 0) {
			addError("No attached appenders found.");
			return;
		}
		if (queueSize < 1) {
			addError("Invalid queue size [" + queueSize + "]");
			return;
		}
		addInfo("QueueSize:" + queueSize);
		if (discardingThreshold == UNDEFINED)
			discardingThreshold = queueSize / 5;
		addInfo("Setting discardingThreshold to " + discardingThreshold);

		Executor executor = Executors.newCachedThreadPool();
		disruptor = new Disruptor<LogValueEvent>(LogValueEvent.EVENT_FACTORY,
				queueSize, executor, ProducerType.MULTI,
				new SleepingWaitStrategy());
		disruptor.handleEventsWith(new LogDisruptorEventHandle());
		disruptor.start();
		ringBuffer = disruptor.getRingBuffer();

		super.start();
	}

	@Override
	public void stop() {

		if (!isStarted())
			return;
		disruptor.shutdown();
		// mark this appender as stopped so that Worker can also
		// processPriorToRemoval if it is invoking aii.appendLoopOnAppenders
		// and sub-appenders consume the interruption
		super.stop();
	}

	@Override
	protected boolean isQueueBelowDiscardingThreshold() {
		return (ringBuffer.remainingCapacity() < discardingThreshold);
	}

	/**
	 * Events of level TRACE, DEBUG and INFO are deemed to be discardable.
	 * 
	 * @param event
	 * @return true if the event is of level TRACE, DEBUG or INFO false
	 *         otherwise.
	 */
	@Override
	protected boolean isDiscardable(ILoggingEvent event) {
		Level level = event.getLevel();
		return level.toInt() <= Level.INFO_INT;
	}

	@Override
	protected void preprocess(ILoggingEvent eventObject) {
		eventObject.prepareForDeferredProcessing();
		if (includeCallerData)
			eventObject.getCallerData();
	}

	@Override
	protected void put(ILoggingEvent event) {
		ringBuffer.publishEvent(LogProducerTranslator.TRANSLATOR, event, aai);
	}

	public boolean isIncludeCallerData() {
		return includeCallerData;
	}

	public void setIncludeCallerData(boolean includeCallerData) {
		this.includeCallerData = includeCallerData;
	}

}
