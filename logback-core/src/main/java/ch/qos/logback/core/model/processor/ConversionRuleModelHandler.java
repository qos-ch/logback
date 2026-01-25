/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.model.ConversionRuleModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.pattern.color.ConverterSupplierByClassName;
import ch.qos.logback.core.util.OptionHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConversionRuleModelHandler extends ModelHandlerBase {

    private boolean inError;

    public ConversionRuleModelHandler(Context context) {
        super(context);
    }

    static public ConversionRuleModelHandler makeInstance(Context context, ModelInterpretationContext mic) {
        return new ConversionRuleModelHandler(context);
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {

        ConversionRuleModel conversionRuleModel = (ConversionRuleModel) model;
        String converterClass = conversionRuleModel.getClassName();

        if (OptionHelper.isNullOrEmptyOrAllSpaces(converterClass)) {
            addWarn("Missing className. This should have been caught earlier.");
            inError = true;
            return;
        } else {
            converterClass = mic.getImport(converterClass);
        }

        String conversionWord = conversionRuleModel.getConversionWord();


        try {
            Map<String, Supplier<DynamicConverter>> ruleRegistry = (Map<String, Supplier<DynamicConverter>>) context
                    .getObject(CoreConstants.PATTERN_RULE_REGISTRY_FOR_SUPPLIERS);
            if (ruleRegistry == null) {
                ruleRegistry = new HashMap<>();
                context.putObject(CoreConstants.PATTERN_RULE_REGISTRY_FOR_SUPPLIERS, ruleRegistry);
            }
            // put the new rule into the rule registry
            addInfo("registering conversion word " + conversionWord + " with class [" + converterClass + "]");
            ConverterSupplierByClassName converterSupplierByClassName = new ConverterSupplierByClassName(conversionWord, converterClass);
            converterSupplierByClassName.setContext(getContext());
            ruleRegistry.put(conversionWord, converterSupplierByClassName);
        } catch (Exception oops) {
            inError = true;
            String errorMsg = "Could not add conversion rule to PatternLayout.";
            addError(errorMsg);
        }



    }
}
