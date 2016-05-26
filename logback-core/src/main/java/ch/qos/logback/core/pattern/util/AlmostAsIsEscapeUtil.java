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

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.rolling.helper.FileNamePattern;

/**
 * This implementation is intended for use in {@link FileNamePattern}.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class AlmostAsIsEscapeUtil extends RestrictedEscapeUtil {

    /**
     * Do not perform any character escaping, except for '%', and ')'.
     * 
     * <p>
     * Here is the rationale. First, filename patterns do not include escape
     * combinations such as \r or \n. Moreover, characters which have special
     * meaning in logback parsers, such as '{', or '}' cannot be part of file
     * names (so me thinks). The left parenthesis character has special meaning
     * only if it is preceded by %. Thus, the only characters that needs escaping
     * are '%' and ')'.
     * 
     * <p>
     * Note that this method assumes that it is called after the escape character
     * has been consumed.
     */
    public void escape(String escapeChars, StringBuffer buf, char next, int pointer) {
        super.escape("" + CoreConstants.PERCENT_CHAR + CoreConstants.RIGHT_PARENTHESIS_CHAR, buf, next, pointer);
    }
}
