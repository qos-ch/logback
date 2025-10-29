/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.CoreConstants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LegacyPatternLayoutTest {

    LoggerContext context = new LoggerContext();

    /**
     * Test backward compatibility for classes derived from
     * PatternLayout that add custom conversion words.
     */
    @Test public void subPattern() {
        SubPatternLayout layout = new SubPatternLayout();
        layout.setPattern("%"+SubPatternLayout.DOOO);
        layout.setContext(context);
        layout.start();
        LoggingEvent event = new LoggingEvent();
        event.setTimeStamp(0);
        event.setLevel(Level.INFO);

        String result = layout.doLayout(event);
        assertEquals("INFO", result);
    }

    @Test
    public void fromContext() {
        Map<String, String> registry = (Map<String, String>) this.context
                        .getObject(CoreConstants.PATTERN_RULE_REGISTRY);
        //
        assertNull(registry);
        if(registry == null) {
            registry = new HashMap<String, String>();
            this.context.putObject(CoreConstants.PATTERN_RULE_REGISTRY, registry);
        }

        registry.put("legacy", LevelConverter.class.getName());

        PatternLayout patternLayout = new PatternLayout();
        patternLayout.setPattern("%legacy");
        patternLayout.setContext(context);
        patternLayout.start();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.WARN);
        String result = patternLayout.doLayout(event);
        assertEquals("WARN", result);
    }

}
