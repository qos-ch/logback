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

package ch.qos.logback.core.model.util;

import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.spi.ContextAwarePropertyContainer;
import ch.qos.logback.core.util.ContextUtil;
import ch.qos.logback.core.util.OptionHelper;

import java.util.Properties;

/**
 *
 *
 * @since 1.5.1
 */
public class PropertyModelUtil {


    public static boolean checkFileAttributeSanity(PropertyModel propertyModel) {
        String file = propertyModel.getFile();
        String name = propertyModel.getName();
        String value = propertyModel.getValue();
        String resource = propertyModel.getResource();

        return !(OptionHelper.isNullOrEmptyOrAllSpaces(file)) && (OptionHelper.isNullOrEmptyOrAllSpaces(name)
                && OptionHelper.isNullOrEmptyOrAllSpaces(value) && OptionHelper.isNullOrEmptyOrAllSpaces(resource));
    }

    public static boolean checkResourceAttributeSanity(PropertyModel propertyModel) {
        String file = propertyModel.getFile();
        String name = propertyModel.getName();
        String value = propertyModel.getValue();
        String resource = propertyModel.getResource();

        return !(OptionHelper.isNullOrEmptyOrAllSpaces(resource)) && (OptionHelper.isNullOrEmptyOrAllSpaces(name)
                && OptionHelper.isNullOrEmptyOrAllSpaces(value) && OptionHelper.isNullOrEmptyOrAllSpaces(file));
    }

    public static boolean checkValueNameAttributesSanity(PropertyModel propertyModel) {
        String file = propertyModel.getFile();
        String name = propertyModel.getName();
        String value = propertyModel.getValue();
        String resource = propertyModel.getResource();
        return (!(OptionHelper.isNullOrEmptyOrAllSpaces(name) || OptionHelper.isNullOrEmptyOrAllSpaces(value))
                && (OptionHelper.isNullOrEmptyOrAllSpaces(file) && OptionHelper.isNullOrEmptyOrAllSpaces(resource)));
    }

    /**
     * Add all the properties found in the argument named 'props' to an
     * InterpretationContext.
     */
    static public void setProperty(ContextAwarePropertyContainer capc, String key, String value, ActionUtil.Scope scope) {
        switch (scope) {
        case LOCAL:
            capc.addSubstitutionProperty(key, value);
            break;
        case CONTEXT:
            capc.getContext().putProperty(key, value);
            break;
        case SYSTEM:
            OptionHelper.setSystemProperty(capc, key, value);
        }
    }

    /**
     * Add all the properties found in the argument named 'props' to an
     * InterpretationContext.
     */
    static public void setProperties(ContextAwarePropertyContainer capc, Properties props, ActionUtil.Scope scope) {
        switch (scope) {
        case LOCAL:
            capc.addSubstitutionProperties(props);
            break;
        case CONTEXT:
            ContextUtil cu = new ContextUtil(capc.getContext());
            cu.addProperties(props);
            break;
        case SYSTEM:
            OptionHelper.setSystemProperties(capc, props);
        }
    }
}
