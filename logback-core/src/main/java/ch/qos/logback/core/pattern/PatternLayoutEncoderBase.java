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
package ch.qos.logback.core.pattern;

import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

public class PatternLayoutEncoderBase<E> extends LayoutWrappingEncoder<E> {

    String pattern;

    // due to popular demand outputPatternAsHeader is set to false by default
    protected boolean outputPatternAsHeader = false;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean isOutputPatternAsHeader() {
        return outputPatternAsHeader;
    }

    /**
     * Print the pattern string as a header in log files
     *
     * @param outputPatternAsHeader
     * @since 1.0.3
     */
    public void setOutputPatternAsHeader(boolean outputPatternAsHeader) {
        this.outputPatternAsHeader = outputPatternAsHeader;
    }

    public boolean isOutputPatternAsPresentationHeader() {
        return outputPatternAsHeader;
    }

    /**
     * @deprecated replaced by {@link #setOutputPatternAsHeader(boolean)}
     */
    public void setOutputPatternAsPresentationHeader(boolean outputPatternAsHeader) {
        addWarn("[outputPatternAsPresentationHeader] property is deprecated. Please use [outputPatternAsHeader] option instead.");
        this.outputPatternAsHeader = outputPatternAsHeader;
    }

    @Override
    public void setLayout(Layout<E> layout) {
        throw new UnsupportedOperationException("one cannot set the layout of " + this.getClass().getName());
    }

}
