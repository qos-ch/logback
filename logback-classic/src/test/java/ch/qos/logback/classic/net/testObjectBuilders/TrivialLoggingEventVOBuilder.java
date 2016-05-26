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
package ch.qos.logback.classic.net.testObjectBuilders;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.LoggingEventVO;

/**
 * @author Pierre Queinnec
 */
public class TrivialLoggingEventVOBuilder implements Builder {

    public Object build(int i) {
        TrivialLoggingEventBuilder loggingEventBuilder = new TrivialLoggingEventBuilder();
        LoggingEvent event = (LoggingEvent) loggingEventBuilder.build(i);

        return LoggingEventVO.build(event);
    }

}
