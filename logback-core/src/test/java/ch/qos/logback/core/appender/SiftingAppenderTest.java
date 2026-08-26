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
package ch.qos.logback.core.appender;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.sift.DefaultDiscriminator;
import ch.qos.logback.core.sift.SiftingAppenderBase;
import ch.qos.logback.core.status.Status;

/**
 * Basic tests of SiftingAppender.
 */
public class SiftingAppenderTest extends AbstractAppenderTest<Object> {
    
    @Test
    public void testNoAppenderConfiuration() {
        final ContextBase context = new ContextBase();
        final SiftingAppenderBase<Object> appender = getAppender();
        appender.setContext(context);
        
        appender.start();
        
        Assertions.assertEquals(2, context.getStatusManager().getCount());
        final List<Status> statuses = context.getStatusManager().getCopyOfStatusList();
        Assertions.assertEquals("Missing discriminator. Aborting", statuses.get(0).getMessage());
        Assertions.assertEquals("AppenderFactory has not been set. Aborting", statuses.get(1).getMessage());
    }

    @Test
    public void testAppenderConfiurationWhenDiscriminatorIsNotStarted() {
        final ContextBase context = new ContextBase();
        final SiftingAppenderBase<Object> appender = getAppender();
        appender.setContext(context);
        appender.setDiscriminator(new DefaultDiscriminator<>());
        
        appender.start();
        
        Assertions.assertEquals(2, context.getStatusManager().getCount());
        final List<Status> statuses = context.getStatusManager().getCopyOfStatusList();
        Assertions.assertEquals("Discriminator has not started successfully. Aborting", statuses.get(0).getMessage());
        Assertions.assertEquals("AppenderFactory has not been set. Aborting", statuses.get(1).getMessage());
    }
    
    @Override
    protected SiftingAppenderBase<Object> getAppender() {
        return new SiftingAppenderBase<Object>() {

            @Override
            protected long getTimestamp(Object event) {
                return 0;
            }

            @Override
            protected boolean eventMarksEndOfLife(Object event) {
                return false;
            }
        };
    }
    
    @Disabled("SiftingAppender does not call 'super.stop()', and I don't know why")
    @Test
    public void testConfiguredAppender() {
        
    }

    @Override
    protected Appender<Object> getConfiguredAppender() {
        throw new UnsupportedOperationException("Implement it while fixing 'testConfiguredAppender'");
    }
}
