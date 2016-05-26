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
package ch.qos.logback.core.rolling.helper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.ConverterUtil;
import ch.qos.logback.core.pattern.LiteralConverter;
import ch.qos.logback.core.pattern.parser.Node;
import ch.qos.logback.core.pattern.parser.Parser;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.pattern.util.AlmostAsIsEscapeUtil;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * After parsing file name patterns, given a number or a date, instances of this
 * class can be used to compute a file name according to the file name pattern
 * and the given integer or date.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class FileNamePattern extends ContextAwareBase {

    static final Map<String, String> CONVERTER_MAP = new HashMap<String, String>();
    static {
        CONVERTER_MAP.put(IntegerTokenConverter.CONVERTER_KEY, IntegerTokenConverter.class.getName());
        CONVERTER_MAP.put(DateTokenConverter.CONVERTER_KEY, DateTokenConverter.class.getName());
    }

    String pattern;
    Converter<Object> headTokenConverter;

    public FileNamePattern(String patternArg, Context contextArg) {
        // the pattern is slashified
        setPattern(FileFilterUtil.slashify(patternArg));
        setContext(contextArg);
        parse();
        ConverterUtil.startConverters(this.headTokenConverter);
    }

    void parse() {
        try {
            // http://jira.qos.ch/browse/LBCORE-130
            // we escape ')' for parsing purposes. Note that the original pattern is preserved
            // because it is shown to the user in status messages. We don't want the escaped version
            // to leak out.
            String patternForParsing = escapeRightParantesis(pattern);
            Parser<Object> p = new Parser<Object>(patternForParsing, new AlmostAsIsEscapeUtil());
            p.setContext(context);
            Node t = p.parse();
            this.headTokenConverter = p.compile(t, CONVERTER_MAP);

        } catch (ScanException sce) {
            addError("Failed to parse pattern \"" + pattern + "\".", sce);
        }
    }

    String escapeRightParantesis(String in) {
        return pattern.replace(")", "\\)");
    }

    public String toString() {
        return pattern;
    }

    public DateTokenConverter<Object> getPrimaryDateTokenConverter() {
        Converter<Object> p = headTokenConverter;

        while (p != null) {
            if (p instanceof DateTokenConverter) {
                DateTokenConverter<Object> dtc = (DateTokenConverter<Object>) p;
                // only primary converters should be returned as
                if (dtc.isPrimary())
                    return dtc;
            }

            p = p.getNext();
        }

        return null;
    }

    public IntegerTokenConverter getIntegerTokenConverter() {
        Converter<Object> p = headTokenConverter;

        while (p != null) {
            if (p instanceof IntegerTokenConverter) {
                return (IntegerTokenConverter) p;
            }

            p = p.getNext();
        }
        return null;
    }

    public boolean hasIntegerTokenCOnverter() {
        IntegerTokenConverter itc = getIntegerTokenConverter();
        return itc != null;
    }
    
    public String convertMultipleArguments(Object... objectList) {
        StringBuilder buf = new StringBuilder();
        Converter<Object> c = headTokenConverter;
        while (c != null) {
            if (c instanceof MonoTypedConverter) {
                MonoTypedConverter monoTyped = (MonoTypedConverter) c;
                for (Object o : objectList) {
                    if (monoTyped.isApplicable(o)) {
                        buf.append(c.convert(o));
                    }
                }
            } else {
                buf.append(c.convert(objectList));
            }
            c = c.getNext();
        }
        return buf.toString();
    }

    public String convert(Object o) {
        StringBuilder buf = new StringBuilder();
        Converter<Object> p = headTokenConverter;
        while (p != null) {
            buf.append(p.convert(o));
            p = p.getNext();
        }
        return buf.toString();
    }

    public String convertInt(int i) {
        return convert(i);
    }

    public void setPattern(String pattern) {
        if (pattern != null) {
            // Trailing spaces in the pattern are assumed to be undesired.
            this.pattern = pattern.trim();
        }
    }

    public String getPattern() {
        return pattern;
    }

    /**
     * Given date, convert this instance to a regular expression.
     *
     * Used to compute sub-regex when the pattern has both %d and %i, and the
     * date is known.
     */
    public String toRegexForFixedDate(Date date) {
        StringBuilder buf = new StringBuilder();
        Converter<Object> p = headTokenConverter;
        while (p != null) {
            if (p instanceof LiteralConverter) {
                buf.append(p.convert(null));
            } else if (p instanceof IntegerTokenConverter) {
                buf.append("(\\d{1,3})");
            } else if (p instanceof DateTokenConverter) {
                buf.append(p.convert(date));
            }
            p = p.getNext();
        }
        return buf.toString();
    }

    /**
     * Given date, convert this instance to a regular expression
     */
    public String toRegex() {
        StringBuilder buf = new StringBuilder();
        Converter<Object> p = headTokenConverter;
        while (p != null) {
            if (p instanceof LiteralConverter) {
                buf.append(p.convert(null));
            } else if (p instanceof IntegerTokenConverter) {
                buf.append("\\d{1,2}");
            } else if (p instanceof DateTokenConverter) {
                DateTokenConverter<Object> dtc = (DateTokenConverter<Object>) p;
                buf.append(dtc.toRegex());
            }
            p = p.getNext();
        }
        return buf.toString();
    }
}
