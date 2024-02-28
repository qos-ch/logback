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
package ch.qos.logback.core.model;

import ch.qos.logback.core.CoreConstants;

public class ModelConstants {

    
    public static final String DEBUG_SYSTEM_PROPERTY_KEY = "logback.debug";
    public static final String NULL_STR = CoreConstants.NULL_STR;

    public static final String INVALID_ATTRIBUTES = "In <property> element, either the \"file\" attribute alone, or "
            + "the \"resource\" element alone, or both the \"name\" and \"value\" attributes must be set.";

    public static final String PARENT_PROPPERTY_KEY = "parent";
}
