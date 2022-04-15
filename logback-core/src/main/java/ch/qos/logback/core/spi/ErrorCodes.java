/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.spi;

public class ErrorCodes {

    
    public static final String EMPTY_MODEL_STACK = "Could not find valid configuration instuctions. Exiting.";
    public static final String PARENT_MODEL_NOT_FOUND = "Could not find parent model.";
    public static final String SKIPPING_IMCICIT_MODEL_ADDITION = " Will not add current implicit model as subModel.";
    
}
