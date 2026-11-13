/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.kryo;

import java.util.List;
import java.util.function.Consumer;

class ListConsumer implements Consumer<LoggingEventKryoVO> {
    List<LoggingEventKryoVO> list;

    ListConsumer(List<LoggingEventKryoVO> list) {
        this.list = list;
    }

    @Override
    public void accept(LoggingEventKryoVO event) {
        list.add(event);
    }
}
