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
package ch.qos.logback.access.sift;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.joran.spi.DefaultClass;
import ch.qos.logback.core.sift.Discriminator;
import ch.qos.logback.core.sift.SiftingAppenderBase;

/**
 * This appender can contains other appenders which it can build dynamically
 * depending on MDC values. The built appender is specified as part of a
 * configuration file.
 * 
 * <p>See the logback manual for further details.
 * 
 * 
 * @author Ceki Gulcu
 */
public class SiftingAppender extends SiftingAppenderBase<IAccessEvent> {

    @Override
    public void start() {
        super.start();
    }

    @Override
    protected long getTimestamp(IAccessEvent event) {
        return event.getTimeStamp();
    }

    @Override
    protected boolean eventMarksEndOfLife(IAccessEvent event) {
        return false;
    }

    @Override
    @DefaultClass(AccessEventDiscriminator.class)
    public void setDiscriminator(Discriminator<IAccessEvent> discriminator) {
        super.setDiscriminator(discriminator);
    }
}
