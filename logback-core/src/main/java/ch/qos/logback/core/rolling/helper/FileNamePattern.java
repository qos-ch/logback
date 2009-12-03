/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
import ch.qos.logback.core.pattern.parser.ScanException;
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
    CONVERTER_MAP.put(IntegerTokenConverter.CONVERTER_KEY,
        IntegerTokenConverter.class.getName());
    CONVERTER_MAP.put(DateTokenConverter.CONVERTER_KEY,
        DateTokenConverter.class.getName());
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
      Parser<Object> p = new Parser<Object>(pattern, new AlmostAsIsEscapeUtil());
      p.setContext(context);
      Node t = p.parse();
      this.headTokenConverter = p.compile(t, CONVERTER_MAP);

    } catch (ScanException sce) {
      addError("Failed to parse pattern \"" + pattern + "\".", sce);
    }
  }

  public String toString() {
    return pattern;
  }

  public DateTokenConverter getDateTokenConverter() {
    Converter p = headTokenConverter;

    while (p != null) {
      if (p instanceof DateTokenConverter) {
        return (DateTokenConverter) p;
      }

      p = p.getNext();
    }

    return null;
  }

  public IntegerTokenConverter getIntegerTokenConverter() {
    Converter p = headTokenConverter;

    while (p != null) {
      if (p instanceof IntegerTokenConverter) {
        return (IntegerTokenConverter) p;
      }

      p = p.getNext();
    }
    return null;
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
    Integer integerArg = new Integer(i);
    return convert(integerArg);
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
   * Given date, convert this instance to a  regular expression.
    */
  public String toRegex(Date date) {
    StringBuilder buf = new StringBuilder();
    Converter<Object> p = headTokenConverter;
    while (p != null) {
      if (p instanceof LiteralConverter) {
        buf.append(p.convert(null));
      } else if (p instanceof IntegerTokenConverter) {
        buf.append("(\\d{1,2})");
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
