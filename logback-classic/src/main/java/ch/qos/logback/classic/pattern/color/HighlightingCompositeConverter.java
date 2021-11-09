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
package ch.qos.logback.classic.pattern.color;

import static ch.qos.logback.core.pattern.color.ANSIConstants.BLUE_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.BOLD;
import static ch.qos.logback.core.pattern.color.ANSIConstants.DEFAULT_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.RED_FG;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

/**
 * Highlights inner-text depending on the level, in bold red for events of level ERROR, in red for WARN,
 * in BLUE for INFO, and in the default color for other levels.
 */
public class HighlightingCompositeConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {

    @Override
    protected String getForegroundColorCode(final ILoggingEvent event) {
        final Level level = event.getLevel();
        switch (level.toInt()) {
        case Level.ERROR_INT:
            return BOLD + RED_FG;
        case Level.WARN_INT:
            return RED_FG;
        case Level.INFO_INT:
            return BLUE_FG;
        default:
            return DEFAULT_FG;
        }

    }
}
