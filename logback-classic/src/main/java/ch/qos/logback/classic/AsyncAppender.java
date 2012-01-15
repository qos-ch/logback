/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This {@link Appender} logs {@link ILoggingEvent}s asynchronously. It acts solely as an event dispatcher and must
 * therefore be attached to one or more child appenders in order to do useful logging. It is the user's responsibility
 * to close appenders, typically at the end of the application lifecycle.
 * <p>
 * The appender buffers events in a {@link BlockingQueue} on the application thread(s). The appenders {@link Dispatcher}
 * thread takes events from the head of the queue, and dispatches them to all the appenders that are attached to this
 * appender. With the method {@link #getQueueSize()} the number of events currently stored in the event queue can be
 * retrieved.
 * <p>
 * The appenders event queue is configured with a maximum capacity of {@link #DEFAULT_QUEUE_CAPACITY 1000} (defined by
 * the variable {@link #setQueueCapacity(int) QueueCapacity}). If the queue is filled up, then application threads are
 * blocked from logging new events until the dispatcher thread has had a chance to dispatch one or more events. When the
 * queue is no longer at its maximum configured capacity, application threads are able to start logging events once
 * more. Asynchronous logging therefore becomes pseudo-synchronous when the appender is operating at or near the
 * capacity of its event buffer. This is not necessarily a bad thing. It's the price a threaded application will have to
 * pay sometimes. The appender is designed to allow the application to keep on running, albeit taking slightly more time
 * to log events until the pressure on the appenders buffer eases.
 * <p>
 * Optimally tuning the size of the appenders event queue for maximum application throughput depends upon several
 * factors. Any or all of the following factors are likely to cause pseudo-synchronous behavior to be exhibited:
 * <ul>
 * <li>Large numbers of application threads</li>
 * <li>Large numbers of logging events per application call</li>
 * <li>Large amounts of data per logging event</li>
 * <li>High latency of child appenders</li>
 * </ul>
 * To keep things moving, increasing the size of the appenders queue will generally help, at the expense of heap
 * available to the application when large numbers of logging events are queued.
 * <p>
 * It's possible to attach separate instances of this appender to one another to achieve multi-threaded dispatch. For
 * example, assume a requirement for two child appenders that each perform relatively expensive operations such as
 * messaging and file IO. For this case, one could set up a graph of appenders such that the parent asynchronous
 * appender, A, has two child asynchronous appenders attached, B and C. Let's say B in turn has a child file I/O
 * appender attached, and C has a child messaging appender attached. This will result in fast dispatch from A to both B
 * and C via As dispatch thread. Bs dispatch thread will be dedicated to logging to file I/O, and Cs dispatch thread
 * will be dedicated to logging via messaging.
 * <p>
 * Due to performance reasons the "expensive" caller data associated with an event is omitted while the event is
 * prepared before being added to the event queue. By default only "cheap" data like the applications thread name and
 * the <a href="http://logback.qos.ch/manual/mdc.html">MDC</a> are copied. To configure the appender to copy the caller
 * data as well, set the variable {@link #setIncludeCallerData(boolean) IncludeCallerData} to <code>true</code>.
 * <p>
 * <u>Sample configuration:</u>
 * <p>
 * <code>
 * &lt;appender name=&quot;ASYNC&quot;
 * class=&quot;ch.qos.logback.classic.AsyncAppender&quot;&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;QueueCapacity&quot; value=&quot;5000&quot;/&gt; &lt;!--
 * Default is 1000 --&gt;<br/>
 * &nbsp;&nbsp;&lt;param name=&quot;IncludeCallerData&quot; value=&quot;true&quot;/&gt; &lt;!--
 * Default is false --&gt;<br/>
 * &nbsp;&nbsp;&lt;appender-ref ref=&quot;STDOUT&quot;/&gt;<br/>
 * &lt;/appender&gt;
 * </code>
 * @author Torsten Juergeleit
 * @since 0.9.19
 */
public final class AsyncAppender<E> extends UnsynchronizedAppenderBase<E> implements AppenderAttachable<E> {

    public static final String DISPATCHER_THREAD_NAME_PREFIX = "Logback AsyncAppender Dispatcher";

    public static final int DEFAULT_QUEUE_CAPACITY = 1000;
    public static final boolean DEFAULT_INCLUDE_CALLER_DATA = false;
    public static final boolean DEFAULT_BLOCK_WHEN_FULL = false;

    /**
     * The internally used queue is bound to this number of {@link LoggingEvent}s.
     * <p>
     * The <code>QueueCapacity</code> variable is set to <code>1000</code> by default.
     */
    private int queueCapacity = DEFAULT_QUEUE_CAPACITY;

    /**
     * Before queuing a {@link LoggingEvent} the caller data is retrieved from the application thread.
     * <p>
     * <b>This operation is expensive (a stack trace is created). So don't use it if the caller data is not logged.</b>
     * <p>
     * The <code>IncludeCallerData</code> variable is set to <code>false</code> by default.
     */
    private boolean includeCallerData = DEFAULT_INCLUDE_CALLER_DATA;

    /**
     * When the internal queue has reach the capacity indicated by the <code>QueueCapacity</code> variable, this flag
     * determines what should happen to all subsequent {@link LoggingEvent} received by the appender.  If the flag is
     * set to true the appender will block until space becomes available.  If set to false the {@link LoggingEvent}
     * objects will be dropped.
     * <p>
     * The <code>BlockWhenFull</code> variable is set to <code>false</code> by default.
     */
    private boolean blockWhenFull = DEFAULT_BLOCK_WHEN_FULL;

    /** The appenders we are forwarding events to */
    private final AppenderAttachableImpl<E> appenders = new AppenderAttachableImpl<E>();

    /** Queue that is used to forward events to the dispatcher thread */
    private BlockingQueue<E> queue;

    /**
     * {@link Runnable} which forwards {@link LoggingEvent}s to the attached appenders
     */
    private Dispatcher<E> dispatcher;

    /** Thread running the {@link LoggingEvent} {@link Dispatcher} */
    private Thread dispatcherThread;

    private static final int MAX_EXCEPTION_REPEATS = 3;
    private int exceptionRepeatCount = 0;

    /**
     * The default constructor does nothing.
     */
    public AsyncAppender() {}

    /**
     * Sets the capacity of the event queue. The default value is 1000.
     * @param queueCapacity must be within the range of 1 and {@link Integer.MAX_VALUE}
     */
    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    /**
     * Specifies if an events caller data should be calculated before the event is added to the event queue. The default
     * value is <code>false</code>.
     * @param includeCallerData if <code>true</code> then caller data is calculated
     */
    public void setIncludeCallerData(boolean includeCallerData) {
        this.includeCallerData = includeCallerData;
    }

    /**
     * Specifies if the appender should block when the queue backing the appender becomes full. The default value is
     * <code>false</code>.
     * @param blockWhenFull if <code>true</code> the appender will block
     */
    public void setBlockWhenFull(boolean blockWhenFull) {
        this.blockWhenFull = blockWhenFull;
    }

    /**
     * Returns the number of {@link LoggingEvent}s currently stored in the appenders event queue.
     * @return current queue size or <code>-1</code> if the appender is not started
     */
    public int getQueueSize() {
        return (super.isStarted() ? queue.size() : -1);
    }

    @Override
    public void start() {
        if (!super.isStarted()) {

            // Start all attached appenders
            Iterator<Appender<E>> iter = appenders.iteratorForAppenders();
            int size = 0;
            while (iter.hasNext()) {
                iter.next().start();
                size++;
            }
            if (size == 0) {
                addError("No appender configured");
                return;
            }

            // Initialize event queue
            if (queueCapacity < 1 || queueCapacity > Integer.MAX_VALUE) {
                addError("Invalid queue capacity of " + queueCapacity);
                return;
            }
            queue = new LinkedBlockingQueue<E>(queueCapacity);

            // Initialize event dispatcher thread
            dispatcher = new Dispatcher<E>(this, appenders, queue);
            dispatcherThread = new Thread(dispatcher);
            dispatcherThread.setName(DISPATCHER_THREAD_NAME_PREFIX + " [" + getName() + "] - " + dispatcherThread.getName());
            dispatcherThread.setDaemon(true);
            dispatcherThread.start();
            super.start();
        }
    }

    @Override
    public void stop() {
        if (super.isStarted()) {
            super.stop();

            // Tell the dispatcher we want to stop
            dispatcher.stop();

            // Interrupt the dispatcher thread to allow it to exit if it's blocking on
            // the queue
            dispatcherThread.interrupt();
            try {
                dispatcherThread.join();
            } catch (InterruptedException e) {
                addError("We're interrupted while waiting for the " + "dispatcher thread to finish", e);
            }

            // Stop all attached appenders
            Iterator<Appender<E>> iter = appenders.iteratorForAppenders();
            while (iter.hasNext()) {
                iter.next().stop();
            }
        }
    }

    @Override
    protected void append(E event) {
        if (!dispatcher.isStopRequested() && dispatcherThread.isAlive()) {

            // Populates the event with information from caller thread
            if (event instanceof ILoggingEvent) {
                ILoggingEvent le = (ILoggingEvent)event;
                le.prepareForDeferredProcessing();
                if (includeCallerData) {
                    le.getCallerData();
                }
            }

            // Queue up the event -- it will be processed by the dispatcher
            // thread
            try {
                if (!queue.offer(event)) {
                    addInfo("Blocking queue is full");
                    if (blockForEvent(event)) {
                        addInfo("Blocking for event");
                        queue.put(event);
                    }
                }
            } catch (InterruptedException e) {
                if (exceptionRepeatCount++ < MAX_EXCEPTION_REPEATS) {
                    addError("Error while adding event to the work queue", e);
                }
            }
        } else {

            // If the dispatcher is no longer running, handle events
            // synchronously
            appenders.appendLoopOnAppenders(event);
        }
    }

    protected boolean blockForEvent(E event) throws InterruptedException {
        // Block if configured to do so, drop otherwise
        return blockWhenFull;
    }

    public void addAppender(Appender<E> newAppender) {
        appenders.addAppender(newAppender);
    }

    public Iterator<Appender<E>> iteratorForAppenders() {
        return appenders.iteratorForAppenders();
    }

    public Appender<E> getAppender(String name) {
        return appenders.getAppender(name);
    }

    public boolean isAttached(Appender<E> appender) {
        return appenders.isAttached(appender);
    }

    public void detachAndStopAllAppenders() {
        appenders.detachAndStopAllAppenders();
    }

    public boolean detachAppender(Appender<E> appender) {
        return appenders.detachAppender(appender);
    }

    public boolean detachAppender(String name) {
        return appenders.detachAppender(name);
    }

    /**
     * Used by thread to retrieve {@link LoggingEvent}s from a {@link BlockingQueue} in a loop and forward them to
     * attached {@link Appender}s. Loop exits if {@link #stop()} is called.
     */
    private static class Dispatcher<E> implements Runnable {

        private final Appender<E> asyncAppender;
        private final AppenderAttachableImpl<E> appenders;
        private final BlockingQueue<E> queue;
        private int exceptionRepeatCount = 0;

        // Set to true when the dispatcher thread should exit
        private final AtomicBoolean stopRequested = new AtomicBoolean(false);

        private Dispatcher(Appender<E> asyncAppender, AppenderAttachableImpl<E> appenders, BlockingQueue<E> queue) {
            this.asyncAppender = asyncAppender;
            this.appenders = appenders;
            this.queue = queue;
        }

        public void run() {
            do {
                try {
                    E event = queue.take();
                    appenders.appendLoopOnAppenders(event);
                } catch (InterruptedException e) {
                    // We're here ignoring that a taken event may not be consumed by any
                    // appender
                } catch (Exception e) {
                    if (exceptionRepeatCount++ < MAX_EXCEPTION_REPEATS) {
                        asyncAppender.addError("Error while dispatching event", e);
                    }
                }
            } while (!stopRequested.get());
        }

        public void stop() {
            stopRequested.set(true);
        }

        public boolean isStopRequested() {
            return stopRequested.get();
        }
    }
}
