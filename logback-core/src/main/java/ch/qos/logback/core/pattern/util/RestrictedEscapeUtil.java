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
package ch.qos.logback.core.pattern.util;

/**
 * This implementation is intended for use in PatternLayout.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class RestrictedEscapeUtil implements IEscapeUtil {

    @Override
    public void escape(final String escapeChars, final StringBuffer buf, final char next, final int pointer) {
        if (escapeChars.indexOf(next) >= 0) {
        } else {
            // restitute the escape char (because it was consumed
            // before this method was called).
            buf.append("\\");
        }
        buf.append(next);
    }

}
