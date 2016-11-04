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

import static ch.qos.logback.core.CoreConstants.CONTEXT_NAME_KEY;
import static ch.qos.logback.core.CoreConstants.FA_FILENAME_COLLISION_MAP;
import static ch.qos.logback.core.CoreConstants.RFA_FILENAME_PATTERN_COLLISION_MAP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.spi.LogbackLock;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.ExecutorServiceUtil;

public class ContextBase implements Context, LifeCycle {

    private long birthTime = System.currentTimeMillis();

    private String name;
    private StatusManager sm = new BasicStatusManager();
    // TODO propertyMap should be observable so that we can be notified
    // when it changes so that a new instance of propertyMap can be
    // serialized. For the time being, we ignore this shortcoming.
    Map<String, String> propertyMap = new HashMap<String, String>();
    Map<String, Object> objectMap = new HashMap<String, Object>();

    LogbackLock configurationLock = new LogbackLock();

    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;
    protected List<ScheduledFuture<?>> scheduledFutures = new ArrayList<ScheduledFuture<?>>(1);
    private LifeCycleManager lifeCycleManager;
    private boolean started;

    public ContextBase() {
        initCollisionMaps();
    }

    public StatusManager getStatusManager() {
        return sm;
    }

    /**
     * Set the {@link StatusManager} for this context. Note that by default this
     * context is initialized with a {@link BasicStatusManager}. A null value for
     * the 'statusManager' argument is not allowed.
     * <p/>
     * <p> A malicious attacker can set the status manager to a dummy instance,
     * disabling internal error reporting.
     *
     * @param statusManager the new status manager
     */
    public void setStatusManager(StatusManager statusManager) {
        // this method was added in response to http://jira.qos.ch/browse/LBCORE-35
        if (statusManager == null) {
            throw new IllegalArgumentException("null StatusManager not allowed");
        }
        this.sm = statusManager;
    }

    public Map<String, String> getCopyOfPropertyMap() {
        return new HashMap<String, String>(propertyMap);
    }

    public void putProperty(String key, String val) {
        this.propertyMap.put(key, val);
    }

    protected void initCollisionMaps() {
        putObject(FA_FILENAME_COLLISION_MAP, new HashMap<String, String>());
        putObject(RFA_FILENAME_PATTERN_COLLISION_MAP, new HashMap<String, String>());
    }

    /**
     * Given a key, return the corresponding property value. If invoked with
     * the special key "CONTEXT_NAME", the name of the context is returned.
     *
     * @param key
     * @return
     */
    public String getProperty(String key) {
        if (CONTEXT_NAME_KEY.equals(key))
            return getName();

        return (String) this.propertyMap.get(key);
    }

    public Object getObject(String key) {
        return objectMap.get(key);
    }

    public void putObject(String key, Object value) {
        objectMap.put(key, value);
    }

    public void removeObject(String key) {
        objectMap.remove(key);
    }

    public String getName() {
        return name;
    }

    public void start() {
        // We'd like to create the executor service here, but we can't;
        // ContextBase has not always implemented LifeCycle and there are *many*
        // uses (mostly in tests) that would need to be modified.
        started = true;
    }

    public void stop() {
        // We don't check "started" here, because the executor service uses
        // lazy initialization, rather than being created in the start method
        stopExecutorServices();
        
        started = false;
    }

    public boolean isStarted() {
        return started;
    }

    /**
     * Clear the internal objectMap and all properties. Removes registered
     * shutdown hook
     */
    public void reset() {
        
        removeShutdownHook();
        getLifeCycleManager().reset();
        propertyMap.clear();
        objectMap.clear();
    }

    /**
     * The context name can be set only if it is not already set, or if the
     * current name is the default context name, namely "default", or if the
     * current name and the old name are the same.
     *
     * @throws IllegalStateException if the context already has a name, other than "default".
     */
    public void setName(String name) throws IllegalStateException {
        if (name != null && name.equals(this.name)) {
            return; // idempotent naming
        }
        if (this.name == null || CoreConstants.DEFAULT_CONTEXT_NAME.equals(this.name)) {
            this.name = name;
        } else {
            throw new IllegalStateException("Context has been already given a name");
        }
    }

    public long getBirthTime() {
        return birthTime;
    }

    public Object getConfigurationLock() {
        return configurationLock;
    }

    @Override
    /**
     * @deprecated
     */
    public synchronized ExecutorService getExecutorService() {
        return getScheduledExecutorService();
    }

    @Override
    public synchronized ScheduledExecutorService getScheduledExecutorService() {
        if (scheduledExecutorService == null) {
            scheduledExecutorService = ExecutorServiceUtil.newScheduledExecutorService();
        }
        return scheduledExecutorService;
    }

    private synchronized void stopExecutorServices() {
        if (executorService != null) {
            ExecutorServiceUtil.shutdown(executorService);
            executorService = null;
        }
        if (scheduledExecutorService != null) {
            ExecutorServiceUtil.shutdown(scheduledExecutorService);
            scheduledExecutorService = null;
        }
    }

    private void removeShutdownHook() {
        Thread hook = (Thread) getObject(CoreConstants.SHUTDOWN_HOOK_THREAD);
        if (hook != null) {
            removeObject(CoreConstants.SHUTDOWN_HOOK_THREAD);
            try {
                Runtime.getRuntime().removeShutdownHook(hook);
            } catch (IllegalStateException e) {
                // if JVM is already shutting down, ISE is thrown
                // no need to do anything else
            }
        }
    }

    public void register(LifeCycle component) {
        getLifeCycleManager().register(component);
    }

    /**
     * Gets the life cycle manager for this context.
     * <p>
     * The default implementation lazily initializes an instance of
     * {@link LifeCycleManager}.  Subclasses may override to provide a custom 
     * manager implementation, but must take care to return the same manager
     * object for each call to this method.
     * <p>
     * This is exposed primarily to support instrumentation for unit testing.
     * 
     * @return manager object 
     */
    synchronized LifeCycleManager getLifeCycleManager() {
        if (lifeCycleManager == null) {
            lifeCycleManager = new LifeCycleManager();
        }
        return lifeCycleManager;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void addScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        scheduledFutures.add(scheduledFuture);
    }

    public List<ScheduledFuture<?>> getScheduledFutures() {
        return new ArrayList<ScheduledFuture<?>>(scheduledFutures);
    }
    
    

}
