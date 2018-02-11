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

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.spi.PropertyContainer;
import ch.qos.logback.core.spi.SequenceNumberGenerator;
import ch.qos.logback.core.status.StatusManager;

/**
 * A context is the main anchorage point of all logback components.
 * 
 * @author Ceki Gulcu
 * 
 */
public interface Context extends PropertyContainer {

    /**
     * Return the StatusManager instance in use.
     * 
     * @return the {@link StatusManager} instance in use.
     */
    StatusManager getStatusManager();

    /**
     * A Context can act as a store for various objects used by LOGBack
     * components.
     * 
     * @return The object stored under 'key'.
     */
    Object getObject(String key);

    /**
     * Store an object under 'key'. If no object can be found, null is returned.
     * 
     * @param key
     * @param value
     */
    void putObject(String key, Object value);

    /**
     * Get all the properties for this context as a Map. Note that the returned
     * cop might be a copy not the original. Thus, modifying the returned Map will
     * have no effect (on the original.)
     * 
     * @return
     */
    // public Map<String, String> getPropertyMap();
    /**
     * Get the property of this context.
     */
    String getProperty(String key);

    /**
     * Set a property of this context.
     */
    void putProperty(String key, String value);

    /**
     * Get a copy of the property map
     * @return
     * @since 0.9.20
     */
    Map<String, String> getCopyOfPropertyMap();

    /**
     * Contexts are named objects.
     * 
     * @return the name for this context
     */
    String getName();

    /**
     * The name of the context can be set only once.
     * 
     * @param name
     */
    void setName(String name);

    /**
     * The time at which this context was created, expressed in
     * millisecond elapsed since the epoch (1.1.1970).
     * 
     * @return The time as measured when this class was created.
     */
    long getBirthTime();

    /**
     * Object used for synchronization purposes. 
     * INTENDED FOR INTERNAL USAGE.
     */
    Object getConfigurationLock();


    /**
     * Returns the ScheduledExecutorService for this context.
     * @return
     * @since 1.1.7
     */
    // Apparently ScheduledThreadPoolExecutor has limitation where a task cannot be submitted from 
    // within a running task. ThreadPoolExecutor does not have this limitation.
    // This causes tests failures in SocketReceiverTest.testDispatchEventForEnabledLevel and
    // ServerSocketReceiverFunctionalTest.testLogEventFromClient.
    ScheduledExecutorService getScheduledExecutorService();

    /**
     * Every context has an ExecutorService which be invoked to execute certain
     * tasks in a separate thread.
     *
     * @return the executor for this context.
     * @since 1.0.0
     * @deprecated use {@link#getScheduledExecutorService()} instead
     */
    ExecutorService getExecutorService();
    /**
     * Register a component that participates in the context's life cycle.
     * <p>
     * All components registered via this method will be stopped and removed
     * from the context when the context is reset.
     * 
     * @param component the subject component
     */
    void register(LifeCycle component);

    void addScheduledFuture(ScheduledFuture<?> scheduledFuture);

    SequenceNumberGenerator getSequenceNumberGenerator();
    void setSequenceNumberGenerator(SequenceNumberGenerator sequenceNumberGenerator);
    

}
