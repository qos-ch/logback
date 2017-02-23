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

import static ch.qos.logback.core.pattern.color.ANSIConstants.*;

/**
 * Encloses a given set of converter output in gray using the appropriate ANSI
 * escape codes.
 *
 * @param <E>
 * @author  Ceki G&uuml;lc&uuml;
 * @since 1.0.5
 */
public class GrayCompositeConverter<E> extends ForegroundCompositeConverterBase<E> {

    @Override
    protected String getForegroundColorCode(E event) {
        return BOLD + BLACK_FG;
    }
}
