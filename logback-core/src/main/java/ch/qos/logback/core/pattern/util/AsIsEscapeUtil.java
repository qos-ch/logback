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
 * @author Ceki G&uuml;lc&uuml;
 */
public class AsIsEscapeUtil implements IEscapeUtil {

    /**
     * Do not perform any character escaping.
     * <p>
     * Note that this method assumes that it is called after the escape character
     * has been consumed.
     */
    public void escape(String escapeChars, StringBuffer buf, char next, int pointer) {
        // restitute the escape char (because it was consumed
        // before this method was called).
        buf.append("\\");
        // restitute the next character
        buf.append(next);
    }
}
