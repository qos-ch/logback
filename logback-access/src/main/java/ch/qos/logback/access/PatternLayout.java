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
package ch.qos.logback.access;

import ch.qos.logback.access.pattern.ContentLengthConverter;
import ch.qos.logback.access.pattern.DateConverter;
import ch.qos.logback.access.pattern.ElapsedSecondsConverter;
import ch.qos.logback.access.pattern.ElapsedTimeConverter;
import ch.qos.logback.access.pattern.EnsureLineSeparation;
import ch.qos.logback.access.pattern.FullRequestConverter;
import ch.qos.logback.access.pattern.FullResponseConverter;
import ch.qos.logback.access.pattern.LineSeparatorConverter;
import ch.qos.logback.access.pattern.LocalIPAddressConverter;
import ch.qos.logback.access.pattern.LocalPortConverter;
import ch.qos.logback.access.pattern.NAConverter;
import ch.qos.logback.access.pattern.QueryStringConverter;
import ch.qos.logback.access.pattern.RemoteHostConverter;
import ch.qos.logback.access.pattern.RemoteIPAddressConverter;
import ch.qos.logback.access.pattern.RemoteUserConverter;
import ch.qos.logback.access.pattern.RequestAttributeConverter;
import ch.qos.logback.access.pattern.RequestContentConverter;
import ch.qos.logback.access.pattern.RequestCookieConverter;
import ch.qos.logback.access.pattern.RequestHeaderConverter;
import ch.qos.logback.access.pattern.RequestMethodConverter;
import ch.qos.logback.access.pattern.RequestParameterConverter;
import ch.qos.logback.access.pattern.RequestProtocolConverter;
import ch.qos.logback.access.pattern.RequestURIConverter;
import ch.qos.logback.access.pattern.RequestURLConverter;
import ch.qos.logback.access.pattern.ResponseContentConverter;
import ch.qos.logback.access.pattern.ResponseHeaderConverter;
import ch.qos.logback.access.pattern.ServerNameConverter;
import ch.qos.logback.access.pattern.SessionIDConverter;
import ch.qos.logback.access.pattern.StatusCodeConverter;
import ch.qos.logback.access.pattern.ThreadNameConverter;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import ch.qos.logback.core.pattern.color.*;
import ch.qos.logback.core.pattern.parser.Parser;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class is a module-specific implementation of
 * {@link ch.qos.logback.access.PatternLayout} to allow http-specific patterns
 * to be used. The <code>ch.qos.logback.access.PatternLayout</code> provides a
 * way to format the logging output that is just as easy and flexible as the
 * usual <code>PatternLayout</code>.
 * </p>
 * <p/>
 * For more information about this layout, please refer to the online manual at
 * http://logback.qos.ch/manual/layouts.html#AccessPatternLayout
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class PatternLayout extends PatternLayoutBase<IAccessEvent> {

    public static final Map<String, String> defaultConverterMap = new HashMap<String, String>();
    public static final String HEADER_PREFIX = "#logback.access pattern: ";

    public static final String CLF_PATTERN = "%h %l %u [%t] \"%r\" %s %b";
    public static final String CLF_PATTERN_NAME = "common";
    public static final String CLF_PATTERN_NAME_2 = "clf";
    public static final String COMBINED_PATTERN = "%h %l %u [%t] \"%r\" %s %b \"%i{Referer}\" \"%i{User-Agent}\"";
    public static final String COMBINED_PATTERN_NAME = "combined";

    static {
        defaultConverterMap.putAll(Parser.DEFAULT_COMPOSITE_CONVERTER_MAP);

        defaultConverterMap.put("a", RemoteIPAddressConverter.class.getName());
        defaultConverterMap.put("remoteIP", RemoteIPAddressConverter.class.getName());

        defaultConverterMap.put("A", LocalIPAddressConverter.class.getName());
        defaultConverterMap.put("localIP", LocalIPAddressConverter.class.getName());

        defaultConverterMap.put("b", ContentLengthConverter.class.getName());
        defaultConverterMap.put("B", ContentLengthConverter.class.getName());
        defaultConverterMap.put("bytesSent", ContentLengthConverter.class.getName());

        defaultConverterMap.put("h", RemoteHostConverter.class.getName());
        defaultConverterMap.put("clientHost", RemoteHostConverter.class.getName());

        defaultConverterMap.put("H", RequestProtocolConverter.class.getName());
        defaultConverterMap.put("protocol", RequestProtocolConverter.class.getName());

        defaultConverterMap.put("i", RequestHeaderConverter.class.getName());
        defaultConverterMap.put("header", RequestHeaderConverter.class.getName());

        defaultConverterMap.put("I", ThreadNameConverter.class.getName());
        defaultConverterMap.put("threadName", ThreadNameConverter.class.getName());
        
        defaultConverterMap.put("l", NAConverter.class.getName());

        defaultConverterMap.put("m", RequestMethodConverter.class.getName());
        defaultConverterMap.put("requestMethod", RequestMethodConverter.class.getName());

        defaultConverterMap.put("q", QueryStringConverter.class.getName());
        defaultConverterMap.put("queryString", QueryStringConverter.class.getName());
        
        defaultConverterMap.put("r", RequestURLConverter.class.getName());
        defaultConverterMap.put("requestURL", RequestURLConverter.class.getName());

        defaultConverterMap.put("s", StatusCodeConverter.class.getName());
        defaultConverterMap.put("statusCode", StatusCodeConverter.class.getName());

        defaultConverterMap.put("S", SessionIDConverter.class.getName());
        defaultConverterMap.put("sessionID", SessionIDConverter.class.getName());
        
        defaultConverterMap.put("t", DateConverter.class.getName());
        defaultConverterMap.put("date", DateConverter.class.getName());

        defaultConverterMap.put("u", RemoteUserConverter.class.getName());
        defaultConverterMap.put("user", RemoteUserConverter.class.getName());

        defaultConverterMap.put("U", RequestURIConverter.class.getName());
        defaultConverterMap.put("requestURI", RequestURIConverter.class.getName());

        defaultConverterMap.put("v", ServerNameConverter.class.getName());
        defaultConverterMap.put("server", ServerNameConverter.class.getName());

        defaultConverterMap.put("localPort", LocalPortConverter.class.getName());

        defaultConverterMap.put("requestAttribute", RequestAttributeConverter.class.getName());
        defaultConverterMap.put("reqAttribute", RequestAttributeConverter.class.getName());

        defaultConverterMap.put("reqCookie", RequestCookieConverter.class.getName());
        defaultConverterMap.put("requestCookie", RequestCookieConverter.class.getName());

        defaultConverterMap.put("responseHeader", ResponseHeaderConverter.class.getName());

        defaultConverterMap.put("requestParameter", RequestParameterConverter.class.getName());
        defaultConverterMap.put("reqParameter", RequestParameterConverter.class.getName());

        defaultConverterMap.put("requestContent", RequestContentConverter.class.getName());

        defaultConverterMap.put("responseContent", ResponseContentConverter.class.getName());

        defaultConverterMap.put("fullRequest", FullRequestConverter.class.getName());
        defaultConverterMap.put("fullResponse", FullResponseConverter.class.getName());

        defaultConverterMap.put("elapsedTime", ElapsedTimeConverter.class.getName());
        defaultConverterMap.put("D", ElapsedTimeConverter.class.getName());

        defaultConverterMap.put("elapsedSeconds", ElapsedSecondsConverter.class.getName());
        defaultConverterMap.put("T", ElapsedSecondsConverter.class.getName());
        
        defaultConverterMap.put("n", LineSeparatorConverter.class.getName());

        defaultConverterMap.put("black", BlackCompositeConverter.class.getName());
        defaultConverterMap.put("red", RedCompositeConverter.class.getName());
        defaultConverterMap.put("green", GreenCompositeConverter.class.getName());
        defaultConverterMap.put("yellow", YellowCompositeConverter.class.getName());
        defaultConverterMap.put("blue", BlueCompositeConverter.class.getName());
        defaultConverterMap.put("magenta", MagentaCompositeConverter.class.getName());
        defaultConverterMap.put("cyan", CyanCompositeConverter.class.getName());
        defaultConverterMap.put("white", WhiteCompositeConverter.class.getName());
        defaultConverterMap.put("gray", GrayCompositeConverter.class.getName());
        defaultConverterMap.put("boldRed", BoldRedCompositeConverter.class.getName());
        defaultConverterMap.put("boldGreen", BoldGreenCompositeConverter.class.getName());
        defaultConverterMap.put("boldYellow", BoldYellowCompositeConverter.class.getName());
        defaultConverterMap.put("boldBlue", BoldBlueCompositeConverter.class.getName());
        defaultConverterMap.put("boldMagenta", BoldMagentaCompositeConverter.class.getName());
        defaultConverterMap.put("boldCyan", BoldCyanCompositeConverter.class.getName());
        defaultConverterMap.put("boldWhite", BoldWhiteCompositeConverter.class.getName());
    }

    public PatternLayout() {
        // set a default value for pattern
        setPattern(CLF_PATTERN);
        // by default postCompileProcessor the is an EnsureLineSeparation instance
        this.postCompileProcessor = new EnsureLineSeparation();
    }

    /**
     * Returns the default converter map for this instance.
     */
    @Override
    public Map<String, String> getDefaultConverterMap() {
        return defaultConverterMap;
    }

    @Override
    public String doLayout(IAccessEvent event) {
        if (!isStarted()) {
            return null;
        }
        return writeLoopOnConverters(event);
    }

    @Override
    public void start() {
        if (getPattern().equalsIgnoreCase(CLF_PATTERN_NAME) || getPattern().equalsIgnoreCase(CLF_PATTERN_NAME_2)) {
            setPattern(CLF_PATTERN);
        } else if (getPattern().equalsIgnoreCase(COMBINED_PATTERN_NAME)) {
            setPattern(COMBINED_PATTERN);
        }
        super.start();
    }

    @Override
    protected String getPresentationHeaderPrefix() {
        return HEADER_PREFIX;
    }
}
