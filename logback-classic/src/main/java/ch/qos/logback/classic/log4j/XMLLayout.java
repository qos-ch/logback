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
package ch.qos.logback.classic.log4j;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.helpers.Transform;

// Code is based on revision 309623 of org.apache.log4j.xml.XMLLayout dated "Wed
// Jul 31 09:25:14 2002 UTC" as authored by Ceki Gulcu.
// See also http://tinyurl.com/dch9mr

/**
 * 
 * Generates log4j.dtd compliant XML documents.
 * 
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class XMLLayout extends LayoutBase<ILoggingEvent> {

    private final int DEFAULT_SIZE = 256;
    private final int UPPER_LIMIT = 2048;

    private StringBuilder buf = new StringBuilder(DEFAULT_SIZE);
    private boolean locationInfo = false;
    private boolean properties = false;

    @Override
    public void start() {
        super.start();
    }

    /**
     * The <b>LocationInfo</b> option takes a boolean value. By default, it is
     * set to false which means there will be no location information output by
     * this layout. If the the option is set to true, then the file name and line
     * number of the statement at the origin of the log statement will be output.
     * 
     * <p>If you are embedding this layout within an {@link
     * org.apache.log4j.net.SMTPAppender} then make sure to set the
     * <b>LocationInfo</b> option of that appender as well.
     */
    public void setLocationInfo(boolean flag) {
        locationInfo = flag;
    }

    /**
     * Returns the current value of the <b>LocationInfo</b> option.
     */
    public boolean getLocationInfo() {
        return locationInfo;
    }

    /**
     * Sets whether MDC key-value pairs should be output, default false.
     * 
     * @param flag
     *                new value.
     * @since 1.2.15
     */
    public void setProperties(final boolean flag) {
        properties = flag;
    }

    /**
     * Gets whether MDC key-value pairs should be output.
     * 
     * @return true if MDC key-value pairs are output.
     * @since 1.2.15
     */
    public boolean getProperties() {
        return properties;
    }

    /**
     * Formats a {@link ILoggingEvent} in conformity with the log4j.dtd.
     */
    public String doLayout(ILoggingEvent event) {

        // Reset working buffer. If the buffer is too large, then we need a new
        // one in order to avoid the penalty of creating a large array.
        if (buf.capacity() > UPPER_LIMIT) {
            buf = new StringBuilder(DEFAULT_SIZE);
        } else {
            buf.setLength(0);
        }

        // We yield to the \r\n heresy.

        buf.append("<log4j:event logger=\"");
        buf.append(Transform.escapeTags(event.getLoggerName()));
        buf.append("\"\r\n");
        buf.append("             timestamp=\"");
        buf.append(event.getTimeStamp());
        buf.append("\" level=\"");
        buf.append(event.getLevel());
        buf.append("\" thread=\"");
        buf.append(Transform.escapeTags(event.getThreadName()));
        buf.append("\">\r\n");

        buf.append("  <log4j:message>");
        buf.append(Transform.escapeTags(event.getFormattedMessage()));
        buf.append("</log4j:message>\r\n");

        // logback does not support NDC
        // String ndc = event.getNDC();

        IThrowableProxy tp = event.getThrowableProxy();
        if (tp != null) {
            StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
            buf.append("  <log4j:throwable><![CDATA[");
            for (StackTraceElementProxy step : stepArray) {
                buf.append(CoreConstants.TAB);
                buf.append(step.toString());
                buf.append("\r\n");
            }
            buf.append("]]></log4j:throwable>\r\n");
        }

        if (locationInfo) {
            StackTraceElement[] callerDataArray = event.getCallerData();
            if (callerDataArray != null && callerDataArray.length > 0) {
                StackTraceElement immediateCallerData = callerDataArray[0];
                buf.append("  <log4j:locationInfo class=\"");
                buf.append(immediateCallerData.getClassName());
                buf.append("\"\r\n");
                buf.append("                      method=\"");
                buf.append(Transform.escapeTags(immediateCallerData.getMethodName()));
                buf.append("\" file=\"");
                buf.append(Transform.escapeTags(immediateCallerData.getFileName()));
                buf.append("\" line=\"");
                buf.append(immediateCallerData.getLineNumber());
                buf.append("\"/>\r\n");
            }
        }

        /*
         * <log4j:properties> <log4j:data name="name" value="value"/> </log4j:properties>
         */
        if (this.getProperties()) {
            Map<String, String> propertyMap = event.getMDCPropertyMap();

            if ((propertyMap != null) && (propertyMap.size() != 0)) {
                Set<Entry<String, String>> entrySet = propertyMap.entrySet();
                buf.append("  <log4j:properties>");
                for (Entry<String, String> entry : entrySet) {
                    buf.append("\r\n    <log4j:data");
                    buf.append(" name='" + Transform.escapeTags(entry.getKey()) + "'");
                    buf.append(" value='" + Transform.escapeTags(entry.getValue()) + "'");
                    buf.append(" />");
                }
                buf.append("\r\n  </log4j:properties>");
            }
        }

        buf.append("\r\n</log4j:event>\r\n\r\n");

        return buf.toString();
    }

    @Override
    public String getContentType() {
        return "text/xml";
    }

}
