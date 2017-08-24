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
package ch.qos.logback.core.sift;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.util.Duration;

/**
 * This appender serves as the base class for actual SiftingAppenders
 * implemented by the logback-classic and logback-access modules. In a nutshell,
 * a SiftingAppender contains other appenders which it can build dynamically
 * depending on discriminating values supplied by the event currently being
 * processed. The appender to build (dynamically) is specified as part of a
 * configuration file.
 *
 * @author Ceki Gulcu
 */
public abstract class SiftingAppenderBase<E> extends AppenderBase<E> {

    protected AppenderTracker<E> appenderTracker;
    AppenderFactory<E> appenderFactory;
    Duration timeout = new Duration(AppenderTracker.DEFAULT_TIMEOUT);
    int maxAppenderCount = AppenderTracker.DEFAULT_MAX_COMPONENTS;

    Discriminator<E> discriminator;

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public int getMaxAppenderCount() {
        return maxAppenderCount;
    }

    public void setMaxAppenderCount(int maxAppenderCount) {
        this.maxAppenderCount = maxAppenderCount;
    }

    /**
     * This setter is intended to be invoked by SiftAction. Customers have no reason to invoke
     * this method directly.
     */
    public void setAppenderFactory(AppenderFactory<E> appenderFactory) {
        this.appenderFactory = appenderFactory;
    }

    @Override
    public void start() {
        int errors = 0;
        if (discriminator == null) {
            addError("Missing discriminator. Aborting");
            errors++;
        }
        if (!discriminator.isStarted()) {
            addError("Discriminator has not started successfully. Aborting");
            errors++;
        }
        if (appenderFactory == null) {
            addError("AppenderFactory has not been set. Aborting");
            errors++;
        } else {
            appenderTracker = new AppenderTracker<E>(context, appenderFactory);
            appenderTracker.setMaxComponents(maxAppenderCount);
            appenderTracker.setTimeout(timeout.getMilliseconds());
        }
        if (errors == 0) {
            super.start();
        }
    }

    @Override
    public void stop() {
        for (Appender<E> appender : appenderTracker.allComponents()) {
            appender.stop();
        }
    }

    abstract protected long getTimestamp(E event);

    @Override
    protected void append(E event) {
        if (!isStarted()) {
            return;
        }
        String discriminatingValue = discriminator.getDiscriminatingValue(event);
        long timestamp = getTimestamp(event);

        Appender<E> appender = appenderTracker.getOrCreate(discriminatingValue, timestamp);
        // marks the appender for removal as specified by the user
        if (eventMarksEndOfLife(event)) {
            appenderTracker.endOfLife(discriminatingValue);
        }
        appenderTracker.removeStaleComponents(timestamp);
        appender.doAppend(event);
    }

    protected abstract boolean eventMarksEndOfLife(E event);

    public Discriminator<E> getDiscriminator() {
        return discriminator;
    }

    public void setDiscriminator(Discriminator<E> discriminator) {
        this.discriminator = discriminator;
    }

    // sometimes one needs to close a nested appender immediately
    // for example when executing a command which has its own nested appender
    // and the command also cleans up after itself. However, an open file appender
    // will prevent the folder from being deleted
    // see http://www.qos.ch/pipermail/logback-user/2010-March/001487.html

    /**
     * @since 0.9.19
     */
    public AppenderTracker<E> getAppenderTracker() {
        return appenderTracker;
    }

    public String getDiscriminatorKey() {
        if (discriminator != null) {
            return discriminator.getKey();
        } else {
            return null;
        }
    }
}
