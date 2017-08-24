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
package ch.qos.logback.access.html;

import static ch.qos.logback.core.CoreConstants.LINE_SEPARATOR;

import java.util.Map;

import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.html.HTMLLayoutBase;
import ch.qos.logback.core.pattern.Converter;

/**
 * 
 * HTMLLayout outputs events in an HTML table. 
 * <p>
 * The content of the table columns are specified using a conversion pattern. 
 * See {@link ch.qos.logback.access.PatternLayout} for documentation on the
 * available patterns.
 * <p>
 * For more information about this layout, please refer to the online manual at
 * http://logback.qos.ch/manual/layouts.html#AccessHTMLLayout
 * 
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class HTMLLayout extends HTMLLayoutBase<IAccessEvent> {

    /**
     * Default pattern string for log output.
     */
    static final String DEFAULT_CONVERSION_PATTERN = "%h%l%u%t%r%s%b";

    /**
     * Constructs a PatternLayout using the DEFAULT_LAYOUT_PATTERN.
     * 
     */
    public HTMLLayout() {
        pattern = DEFAULT_CONVERSION_PATTERN;
        cssBuilder = new DefaultCssBuilder();
    }

    @Override
    protected Map<String, String> getDefaultConverterMap() {
        return PatternLayout.defaultConverterMap;
    }

    @Override
    public String doLayout(IAccessEvent event) {
        StringBuilder buf = new StringBuilder();
        startNewTableIfLimitReached(buf);

        boolean odd = true;
        if (((counter++) & 1) == 0) {
            odd = false;
        }

        buf.append(LINE_SEPARATOR);
        buf.append("<tr class=\"");
        if (odd) {
            buf.append(" odd\">");
        } else {
            buf.append(" even\">");
        }
        buf.append(LINE_SEPARATOR);

        Converter<IAccessEvent> c = head;
        while (c != null) {
            appendEventToBuffer(buf, c, event);
            c = c.getNext();
        }
        buf.append("</tr>");
        buf.append(LINE_SEPARATOR);

        return buf.toString();
    }

    private void appendEventToBuffer(StringBuilder buf, Converter<IAccessEvent> c, IAccessEvent event) {
        buf.append("<td class=\"");
        buf.append(computeConverterName(c));
        buf.append("\">");
        c.write(buf, event);
        buf.append("</td>");
        buf.append(LINE_SEPARATOR);
    }
}
