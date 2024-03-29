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

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

public class ConversionRuleAction extends Action {
    boolean inError = false;

    /**
     * Instantiates a layout of the given class and sets its name.
     *
     */
    @SuppressWarnings("unchecked")
    public void begin(SaxEventInterpretationContext ec, String localName, Attributes attributes) {
        // Let us forget about previous errors (in this object)
        inError = false;

        String errorMsg;
        String conversionWord = attributes.getValue(JoranConstants.CONVERSION_WORD_ATTRIBUTE);
        String converterClass = attributes.getValue(JoranConstants.CONVERTER_CLASS_ATTRIBUTE);

        if (OptionHelper.isNullOrEmptyOrAllSpaces(conversionWord)) {
            inError = true;
            errorMsg = "No 'conversionWord' attribute in <conversionRule>";
            addError(errorMsg);

            return;
        }

        if (OptionHelper.isNullOrEmptyOrAllSpaces(converterClass)) {
            inError = true;
            errorMsg = "No 'converterClass' attribute in <conversionRule>";
            ec.addError(errorMsg);

            return;
        }

        try {
            Map<String, String> ruleRegistry = (Map<String, String>) context
                    .getObject(CoreConstants.PATTERN_RULE_REGISTRY);
            if (ruleRegistry == null) {
                ruleRegistry = new HashMap<String, String>();
                context.putObject(CoreConstants.PATTERN_RULE_REGISTRY, ruleRegistry);
            }
            // put the new rule into the rule registry
            addInfo("registering conversion word " + conversionWord + " with class [" + converterClass + "]");
            ruleRegistry.put(conversionWord, converterClass);
        } catch (Exception oops) {
            inError = true;
            errorMsg = "Could not add conversion rule to PatternLayout.";
            addError(errorMsg);
        }
    }

    /**
     * Once the children elements are also parsed, now is the time to activate the
     * appender options.
     */
    public void end(SaxEventInterpretationContext ec, String n) {
    }

    public void finish(SaxEventInterpretationContext ec) {
    }
}
