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
package ch.qos.logback.core.pattern.color;

import ch.qos.logback.core.pattern.CompositeConverter;
import static ch.qos.logback.core.pattern.color.ANSIConstants.*;

/**
 * Base class for all foreground color setting composite converters.
 *
 * @param <E>
 * @since 1.0.5
 */
abstract public class ForegroundCompositeConverterBase<E> extends CompositeConverter<E> {

    final private static String SET_DEFAULT_COLOR = ESC_START + "0;" + DEFAULT_FG + ESC_END;

    @Override
    protected String transform(E event, String in) {
        StringBuilder sb = new StringBuilder();
        sb.append(ESC_START);
        sb.append(getForegroundColorCode(event));
        sb.append(ESC_END);
        sb.append(in);
        sb.append(SET_DEFAULT_COLOR);
        return sb.toString();
    }

    /**
     * Derived classes return the foreground color specific to the derived class instance.
     * @return  the foreground color for this instance
     */
    abstract protected String getForegroundColorCode(E event);
}
