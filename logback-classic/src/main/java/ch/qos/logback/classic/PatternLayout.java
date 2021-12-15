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
package ch.qos.logback.classic;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.pattern.*;
import ch.qos.logback.classic.pattern.color.HighlightingCompositeConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import ch.qos.logback.core.pattern.color.*;
import ch.qos.logback.core.pattern.parser.Parser;

/**
 * <p>
 * A flexible layout configurable with pattern string. The goal of this class is
 * to {@link #format format} a {@link ILoggingEvent} and return the results in a
 * {#link String}. The format of the result depends on the
 * <em>conversion pattern</em>.
 * <p>
 * For more information about this layout, please refer to the online manual at
 * http://logback.qos.ch/manual/layouts.html#PatternLayout
 * 
 */

public class PatternLayout extends PatternLayoutBase<ILoggingEvent> {

    public static final Map<String, String> DEFAULT_CONVERTER_MAP = new HashMap<String, String>();
    public static final Map<String, String> CONVERTER_CLASS_TO_KEY_MAP = new HashMap<String, String>();
    
    /**
     * @deprecated replaced by DEFAULT_CONVERTER_MAP
     */
    public static final Map<String, String> defaultConverterMap = DEFAULT_CONVERTER_MAP;
    
    public static final String HEADER_PREFIX = "#logback.classic pattern: ";

    static {
        DEFAULT_CONVERTER_MAP.putAll(Parser.DEFAULT_COMPOSITE_CONVERTER_MAP);

        DEFAULT_CONVERTER_MAP.put("d", DateConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("date", DateConverter.class.getName());
        // used by PrefixComposite conventer
        CONVERTER_CLASS_TO_KEY_MAP.put(DateConverter.class.getName(), "date");
        
        DEFAULT_CONVERTER_MAP.put("ms", MicrosecondConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("micros", MicrosecondConverter.class.getName());
        CONVERTER_CLASS_TO_KEY_MAP.put(MicrosecondConverter.class.getName(), "micros");
        
        
        DEFAULT_CONVERTER_MAP.put("r", RelativeTimeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("relative", RelativeTimeConverter.class.getName());
        CONVERTER_CLASS_TO_KEY_MAP.put(RelativeTimeConverter.class.getName(), "relative");
        
        DEFAULT_CONVERTER_MAP.put("level", LevelConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("le", LevelConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("p", LevelConverter.class.getName());
        CONVERTER_CLASS_TO_KEY_MAP.put(LevelConverter.class.getName(), "level");
        
        
        DEFAULT_CONVERTER_MAP.put("t", ThreadConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("thread", ThreadConverter.class.getName());
        CONVERTER_CLASS_TO_KEY_MAP.put(ThreadConverter.class.getName(), "thread");
        
        DEFAULT_CONVERTER_MAP.put("lo", LoggerConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("logger", LoggerConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("c", LoggerConverter.class.getName());
        CONVERTER_CLASS_TO_KEY_MAP.put(LoggerConverter.class.getName(), "logger");
        
        DEFAULT_CONVERTER_MAP.put("m", MessageConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("msg", MessageConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("message", MessageConverter.class.getName());
        CONVERTER_CLASS_TO_KEY_MAP.put(MessageConverter.class.getName(), "message");
        
        DEFAULT_CONVERTER_MAP.put("C", ClassOfCallerConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("class", ClassOfCallerConverter.class.getName());
        CONVERTER_CLASS_TO_KEY_MAP.put(ClassOfCallerConverter.class.getName(), "class");
        
        DEFAULT_CONVERTER_MAP.put("M", MethodOfCallerConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("method", MethodOfCallerConverter.class.getName());
        CONVERTER_CLASS_TO_KEY_MAP.put(MethodOfCallerConverter.class.getName(), "method");
        
        DEFAULT_CONVERTER_MAP.put("L", LineOfCallerConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("line", LineOfCallerConverter.class.getName());
        CONVERTER_CLASS_TO_KEY_MAP.put(LineOfCallerConverter.class.getName(), "line");
        
        DEFAULT_CONVERTER_MAP.put("F", FileOfCallerConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("file", FileOfCallerConverter.class.getName());
        CONVERTER_CLASS_TO_KEY_MAP.put(FileOfCallerConverter.class.getName(), "file");
        
        DEFAULT_CONVERTER_MAP.put("X", MDCConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("mdc", MDCConverter.class.getName());

        DEFAULT_CONVERTER_MAP.put("ex", ThrowableProxyConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("exception", ThrowableProxyConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("rEx", RootCauseFirstThrowableProxyConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("rootException", RootCauseFirstThrowableProxyConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("throwable", ThrowableProxyConverter.class.getName());

        DEFAULT_CONVERTER_MAP.put("xEx", ExtendedThrowableProxyConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("xException", ExtendedThrowableProxyConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("xThrowable", ExtendedThrowableProxyConverter.class.getName());

        DEFAULT_CONVERTER_MAP.put("nopex", NopThrowableInformationConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("nopexception", NopThrowableInformationConverter.class.getName());

        DEFAULT_CONVERTER_MAP.put("cn", ContextNameConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("contextName", ContextNameConverter.class.getName());
        CONVERTER_CLASS_TO_KEY_MAP.put(ContextNameConverter.class.getName(), "contextName");
        
        DEFAULT_CONVERTER_MAP.put("caller", CallerDataConverter.class.getName());
        CONVERTER_CLASS_TO_KEY_MAP.put(CallerDataConverter.class.getName(), "caller");
        
        DEFAULT_CONVERTER_MAP.put("marker", MarkerConverter.class.getName());
        CONVERTER_CLASS_TO_KEY_MAP.put(MarkerConverter.class.getName(), "marker");
        
        DEFAULT_CONVERTER_MAP.put("kvp", KeyValuePairConverter.class.getName());
        CONVERTER_CLASS_TO_KEY_MAP.put(KeyValuePairConverter.class.getName(), "kvp");
        
        
        DEFAULT_CONVERTER_MAP.put("property", PropertyConverter.class.getName());

        DEFAULT_CONVERTER_MAP.put("n", LineSeparatorConverter.class.getName());

        DEFAULT_CONVERTER_MAP.put("black", BlackCompositeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("red", RedCompositeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("green", GreenCompositeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("yellow", YellowCompositeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("blue", BlueCompositeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("magenta", MagentaCompositeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("cyan", CyanCompositeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("white", WhiteCompositeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("gray", GrayCompositeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("boldRed", BoldRedCompositeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("boldGreen", BoldGreenCompositeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("boldYellow", BoldYellowCompositeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("boldBlue", BoldBlueCompositeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("boldMagenta", BoldMagentaCompositeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("boldCyan", BoldCyanCompositeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("boldWhite", BoldWhiteCompositeConverter.class.getName());
        DEFAULT_CONVERTER_MAP.put("highlight", HighlightingCompositeConverter.class.getName());

        DEFAULT_CONVERTER_MAP.put("lsn", LocalSequenceNumberConverter.class.getName());
        CONVERTER_CLASS_TO_KEY_MAP.put(LocalSequenceNumberConverter.class.getName(), "lsn");
        
        DEFAULT_CONVERTER_MAP.put("prefix", PrefixCompositeConverter.class.getName());
        
    }

    public PatternLayout() {
        this.postCompileProcessor = new EnsureExceptionHandling();
    }

    public Map<String, String> getDefaultConverterMap() {
        return DEFAULT_CONVERTER_MAP;
    }

    public String doLayout(ILoggingEvent event) {
        if (!isStarted()) {
            return CoreConstants.EMPTY_STRING;
        }
        return writeLoopOnConverters(event);
    }

    @Override
    protected String getPresentationHeaderPrefix() {
        return HEADER_PREFIX;
    }
}
