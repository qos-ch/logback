/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.core.model;

public class ConversionRuleModel extends ComponentModel {

    String conversionWord;

    @Override
    protected ConversionRuleModel makeNewInstance() {
        return new ConversionRuleModel();
    }

    public String getConversionWord() {
        return conversionWord;
    }

    public void setConversionWord(String conversionWord) {
        this.conversionWord = conversionWord;
    }

    public String getConverterClass() {
        return getClassName();
    }

    public void setConverterClass(String converterClass) {
        setClassName(converterClass);
    }

}
