/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2012, QOS.ch. All rights reserved.
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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;


/**
 * Highlights inner-text depending on the level, in red for levels ERROR and WARN, in BLUE for INFO, and in the default
 * color for other levels.
 */
public class LevelHighlightingCompositeConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {

  @Override
  protected String getForegroundColorCode(ILoggingEvent event) {
    Level level = event.getLevel();
    switch (level.toInt()) {
      case Level.ERROR_INT:
      case Level.WARN_INT: return ANSIConstants.RED_FG;
      case Level.INFO_INT: return ANSIConstants.BLUE_FG;
      default: return ANSIConstants.DEFAULT_FG;
    }

  }
}
