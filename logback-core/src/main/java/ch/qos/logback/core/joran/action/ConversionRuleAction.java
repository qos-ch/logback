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

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.ConversionRuleModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.util.OptionHelper;
import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.Map;

import static ch.qos.logback.core.joran.JoranConstants.CONVERSION_WORD_ATTRIBUTE;

public class ConversionRuleAction extends BaseModelAction {

    static public String CONVERTER_CLASS_ATTRIBUTE = "converterClass";


    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext seic, String name, Attributes attributes) {
        PreconditionValidator pv = new PreconditionValidator(this, seic, name, attributes);

        boolean invalidConverterClassAttribute = pv.isInvalidAttribute(CONVERTER_CLASS_ATTRIBUTE);
        boolean invalidClassAttribute = pv.isInvalidAttribute(CLASS_ATTRIBUTE);

        if(!invalidConverterClassAttribute) {
            pv.addWarn("["+CONVERTER_CLASS_ATTRIBUTE +"] attribute is deprecated and replaced by ["+CLASS_ATTRIBUTE+
                    "]. "+pv.getLocationSuffix());
        }
        boolean missingClass = invalidClassAttribute && invalidConverterClassAttribute;
        if(missingClass) {
            pv.addMissingAttributeError(CLASS_ATTRIBUTE);
            return false;
        }

        boolean multipleClassAttributes = (!invalidClassAttribute) && (!invalidConverterClassAttribute);
        if(multipleClassAttributes) {
            pv.addWarn("Both ["+CONVERTER_CLASS_ATTRIBUTE+"] attribute and ["+CLASS_ATTRIBUTE+"] attribute specified. ");
            pv.addWarn( "["+CLASS_ATTRIBUTE+"] attribute will override. ");
        }
        pv.validateGivenAttribute(CONVERSION_WORD_ATTRIBUTE);
        return pv.isValid();
    }



    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name,
            Attributes attributes) {
        ConversionRuleModel conversionRuleModel = new ConversionRuleModel();
        conversionRuleModel.setConversionWord(attributes.getValue(CONVERSION_WORD_ATTRIBUTE));

        String converterClassStr = attributes.getValue(CONVERTER_CLASS_ATTRIBUTE);
        if(!OptionHelper.isNullOrEmpty(converterClassStr)) {
            conversionRuleModel.setClassName(converterClassStr);
        }
        // if both converterClass and class are specified the latter overrides.
        String classStr = attributes.getValue(CLASS_ATTRIBUTE);
        if(!OptionHelper.isNullOrEmpty(classStr)) {
            conversionRuleModel.setClassName(classStr);
        }
        return conversionRuleModel;
    }

//    /**
//     * Instantiates a layout of the given class and sets its name.
//     *
//     */
//    @SuppressWarnings("unchecked")
//    public void begin(SaxEventInterpretationContext ec, String localName, Attributes attributes) {
//        // Let us forget about previous errors (in this object)
//        inError = false;
//
//        String errorMsg;
//        String conversionWord = attributes.getValue(CONVERSION_WORD_ATTRIBUTE);
//        String converterClass = attributes.getValue(JoranConstants.CONVERTER_CLASS_ATTRIBUTE);
//
//        if (OptionHelper.isNullOrEmptyOrAllSpaces(conversionWord)) {
//            inError = true;
//            errorMsg = "No 'conversionWord' attribute in <conversionRule>";
//            addError(errorMsg);
//
//            return;
//        }
//
//        if (OptionHelper.isNullOrEmptyOrAllSpaces(converterClass)) {
//            inError = true;
//            errorMsg = "No 'converterClass' attribute in <conversionRule>";
//            ec.addError(errorMsg);
//
//            return;
//        }
//
//        try {
//            Map<String, String> ruleRegistry = (Map<String, String>) context
//                    .getObject(CoreConstants.PATTERN_RULE_REGISTRY);
//            if (ruleRegistry == null) {
//                ruleRegistry = new HashMap<String, String>();
//                context.putObject(CoreConstants.PATTERN_RULE_REGISTRY, ruleRegistry);
//            }
//            // put the new rule into the rule registry
//            addInfo("registering conversion word " + conversionWord + " with class [" + converterClass + "]");
//            ruleRegistry.put(conversionWord, converterClass);
//        } catch (Exception oops) {
//            inError = true;
//            errorMsg = "Could not add conversion rule to PatternLayout.";
//            addError(errorMsg);
//        }
//    }
//
//
//    /**
//     * Once the children elements are also parsed, now is the time to activate the
//     * appender options.
//     */
//    public void end(SaxEventInterpretationContext ec, String n) {
//    }
//
//    public void finish(SaxEventInterpretationContext ec) {
//    }
}
