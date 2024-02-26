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

import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.util.OptionHelper;

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
}
