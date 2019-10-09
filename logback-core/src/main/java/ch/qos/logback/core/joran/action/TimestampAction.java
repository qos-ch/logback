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
public class TimestampAction extends BaseModelAction {

    public static final String DATE_PATTERN_ATTRIBUTE = "datePattern";
    public static final String TIME_REFERENCE_ATTRIBUTE = "timeReference";


    @Override
    protected boolean validPreconditions(InterpretationContext interpretationContext, String name, Attributes attributes) {
        boolean valid = true;
        String keyStr = attributes.getValue(KEY_ATTRIBUTE);
        if (OptionHelper.isNullOrEmpty(keyStr)) {
            addError("Attribute named [" + KEY_ATTRIBUTE + "] cannot be empty");
            valid = false;
        }
        String datePatternStr = attributes.getValue(DATE_PATTERN_ATTRIBUTE);
        if (OptionHelper.isNullOrEmpty(datePatternStr)) {
            addError("Attribute named [" + DATE_PATTERN_ATTRIBUTE + "] cannot be empty");
            valid = false;
        }
        return valid;
    }

    
    @Override
    protected Model buildCurrentModel(InterpretationContext interpretationContext, String name, Attributes attributes) {
        TimestampModel timestampModel = new TimestampModel();

        timestampModel.setKey(attributes.getValue(KEY_ATTRIBUTE));
        timestampModel.setDatePattern(attributes.getValue(DATE_PATTERN_ATTRIBUTE));
        timestampModel.setTimeReference(attributes.getValue(TIME_REFERENCE_ATTRIBUTE));
        timestampModel.setScopeStr(attributes.getValue(SCOPE_ATTRIBUTE));
        
        return timestampModel;

    }




}
