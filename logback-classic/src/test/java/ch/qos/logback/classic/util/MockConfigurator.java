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
package ch.qos.logback.classic.util;

import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ConfiguratorRank;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;

@ConfiguratorRank(ConfiguratorRank.CUSTOM_LOW_PRIORITY)
public class MockConfigurator extends ContextAwareBase implements Configurator {

    static Context context = null;

    public ExecutionStatus configure(Context aContext) {
        context = aContext;
        return ExecutionStatus.NEUTRAL;
    }
}
