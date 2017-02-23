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
package ch.qos.logback.core.pattern.parser;

import ch.qos.logback.core.pattern.FormatInfo;

public class FormattingNode extends Node {

    FormatInfo formatInfo;

    FormattingNode(int type) {
        super(type);
    }

    FormattingNode(int type, Object value) {
        super(type, value);
    }

    public FormatInfo getFormatInfo() {
        return formatInfo;
    }

    public void setFormatInfo(FormatInfo formatInfo) {
        this.formatInfo = formatInfo;
    }

    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }

        if (!(o instanceof FormattingNode)) {
            return false;
        }
        FormattingNode r = (FormattingNode) o;

        return (formatInfo != null ? formatInfo.equals(r.formatInfo) : r.formatInfo == null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (formatInfo != null ? formatInfo.hashCode() : 0);
        return result;
    }
}
