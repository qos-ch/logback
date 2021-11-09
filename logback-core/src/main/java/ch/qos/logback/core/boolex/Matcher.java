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
package ch.qos.logback.core.boolex;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

public class Matcher extends ContextAwareBase implements LifeCycle {

    private String regex;
    private String name;
    private boolean caseSensitive = true;
    private boolean canonEq = false;
    private boolean unicodeCase = false;

    private boolean start = false;
    private Pattern pattern;

    public String getRegex() {
        return regex;
    }

    public void setRegex(final String regex) {
        this.regex = regex;
    }

    @Override
    public void start() {
        if (name == null) {
            addError("All Matcher objects must be named");
            return;
        }
        try {
            int code = 0;
            if (!caseSensitive) {
                code |= Pattern.CASE_INSENSITIVE;
            }
            if (canonEq) {
                code |= Pattern.CANON_EQ;
            }
            if (unicodeCase) {
                code |= Pattern.UNICODE_CASE;
            }

            // code |= Pattern.DOTALL;

            pattern = Pattern.compile(regex, code);
            start = true;
        } catch (final PatternSyntaxException pse) {
            addError("Failed to compile regex [" + regex + "]", pse);
        }
    }

    @Override
    public void stop() {
        start = false;
    }

    @Override
    public boolean isStarted() {
        return start;
    }

    // However, this method does
    // not require that the entire region (of the input) be matched.

    /**
     * Checks whether the input matches the regular expression.
     *
     * @param input
     * @return
     * @throws EvaluationException
     */
    public boolean matches(final String input) throws EvaluationException {
        if (start) {
            final java.util.regex.Matcher matcher = pattern.matcher(input);
            return matcher.find();
        }
        throw new EvaluationException("Matcher [" + regex + "] not started");
    }

    public boolean isCanonEq() {
        return canonEq;
    }

    public void setCanonEq(final boolean canonEq) {
        this.canonEq = canonEq;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(final boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isUnicodeCase() {
        return unicodeCase;
    }

    public void setUnicodeCase(final boolean unicodeCase) {
        this.unicodeCase = unicodeCase;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
