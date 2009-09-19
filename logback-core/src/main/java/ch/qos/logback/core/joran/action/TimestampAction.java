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
package ch.qos.logback.core.joran.action;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

/**
 * Given a key and a date-and-time pattern, puts a property to the context, with
 * the specified key and value equal to the current time in the format
 * corresponding to the specified date-and-time pattern.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class TimestampAction extends Action {
  static String DATE_PATTERN_ATTRIBUTE = "datePattern";

  boolean inError = false;

  @Override
  public void begin(InterpretationContext ec, String name, Attributes attributes)
      throws ActionException {
    String keyStr = attributes.getValue(KEY_ATTRIBUTE);
    if (OptionHelper.isEmpty(keyStr)) {
      addError("Attrubute named [" + KEY_ATTRIBUTE + "] cannot be empty");
      inError = true;
    }
    String datePatternStr = attributes.getValue(DATE_PATTERN_ATTRIBUTE);
    if (OptionHelper.isEmpty(datePatternStr)) {
      addError("Attribute named [" + DATE_PATTERN_ATTRIBUTE
          + "] cannot be empty");
      inError = true;
    }

    if (inError)
      return;

    SimpleDateFormat sdf = new SimpleDateFormat(datePatternStr);
    String val = sdf.format(new Date());

    addInfo("Adding property to the context with key=\"" + keyStr
        + "\" and value=\"" + val + "\" to the context");
    context.putProperty(keyStr, val);
  }

  @Override
  public void end(InterpretationContext ec, String name) throws ActionException {
  }

}
