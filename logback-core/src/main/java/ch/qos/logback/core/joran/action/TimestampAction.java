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
package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.TimestampModel;
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

    Object parent;
    TimestampModel timestampModel;

    public static final String DATE_PATTERN_ATTRIBUTE = "datePattern";
    public static final String TIME_REFERENCE_ATTRIBUTE = "timeReference";

    boolean inError = false;

    @Override
    public void begin(InterpretationContext interpretationContext, String name, Attributes attributes) throws ActionException {
        parent = null;

        String keyStr = attributes.getValue(KEY_ATTRIBUTE);
        if (OptionHelper.isEmpty(keyStr)) {
            addError("Attribute named [" + KEY_ATTRIBUTE + "] cannot be empty");
            inError = true;
        }
        String datePatternStr = attributes.getValue(DATE_PATTERN_ATTRIBUTE);
        if (OptionHelper.isEmpty(datePatternStr)) {
            addError("Attribute named [" + DATE_PATTERN_ATTRIBUTE + "] cannot be empty");
            inError = true;
        }

        parent = interpretationContext.peekObject();
        timestampModel = new TimestampModel();

        timestampModel.setKey(keyStr);

        timestampModel.setTag(name);
        timestampModel.setDatePattern(attributes.getValue(DATE_PATTERN_ATTRIBUTE));
        timestampModel.setTimeReference(attributes.getValue(TIME_REFERENCE_ATTRIBUTE));
        timestampModel.setScopeStr(attributes.getValue(SCOPE_ATTRIBUTE));

        if (inError)
            return;

        interpretationContext.pushObject(timestampModel);
    }

    @Override
    public void end(InterpretationContext interpretationContext, String name) throws ActionException {
        Object o = interpretationContext.peekObject();

        if (o != timestampModel) {
            addWarn("The object at the of the stack is not the model [" + timestampModel.getTag() + "] pushed earlier.");
        } else {
            if (this.parent instanceof Model) {
                Model parentModel = (Model) parent;
                parentModel.addSubModel(timestampModel);
            }
            interpretationContext.popObject();
        }
    }

}
