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

package ch.qos.logback.core.pattern.color;

import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.OptionHelper;

import java.util.function.Supplier;

/**
 *
 * <p>Implements the {@link Supplier} interface in order to cater for legacy code using the class name
 * of a converter.
 * </p>
 * <p>Should not be used in non-legacy code.</p>
 */
public class ConverterSupplierByClassName extends ContextAwareBase implements Supplier<DynamicConverter> {

    String conversionWord;
    String converterClassStr;

    public ConverterSupplierByClassName(String conversionWord, String converterClassStr) {
        this.conversionWord = conversionWord;
        this.converterClassStr = converterClassStr;
    }

    @Override
    public DynamicConverter get() {
        try {
            return (DynamicConverter) OptionHelper.instantiateByClassName(converterClassStr, DynamicConverter.class, context);
        } catch (Exception e) {
            addError("Failed to instantiate converter class [" + converterClassStr + "] for conversion word ["+conversionWord+"]", e);
            return null;
        }
    }
}
