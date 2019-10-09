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
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.util.OptionHelper;

public class NewRuleAction extends Action {
    boolean inError = false;

    /**
     * Instantiates an layout of the given class and sets its name.
     */
    public void begin(InterpretationContext ec, String localName, Attributes attributes) {
        // Let us forget about previous errors (in this object)
        inError = false;
        String errorMsg;
        String pattern = attributes.getValue(Action.PATTERN_ATTRIBUTE);
        String actionClass = attributes.getValue(Action.ACTION_CLASS_ATTRIBUTE);

        if (OptionHelper.isNullOrEmpty(pattern)) {
            inError = true;
            errorMsg = "No 'pattern' attribute in <newRule>";
            addError(errorMsg);
            return;
        }

        if (OptionHelper.isNullOrEmpty(actionClass)) {
            inError = true;
            errorMsg = "No 'actionClass' attribute in <newRule>";
            addError(errorMsg);
            return;
        }

        try {
            addInfo("About to add new Joran parsing rule [" + pattern + "," + actionClass + "].");
            ec.getSaxEventInterpreter().getRuleStore().addRule(new ElementSelector(pattern), actionClass);
        } catch (Exception oops) {
            inError = true;
            errorMsg = "Could not add new Joran parsing rule [" + pattern + "," + actionClass + "]";
            addError(errorMsg);
        }
    }

    /**
     * Once the children elements are also parsed, now is the time to activate the
     * appender options.
     */
    public void end(InterpretationContext ec, String n) {
    }

    public void finish(InterpretationContext ec) {
    }
}
