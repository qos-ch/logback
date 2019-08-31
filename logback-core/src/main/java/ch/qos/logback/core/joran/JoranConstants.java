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
package ch.qos.logback.core.joran;

/**
 *
 * This class contains constants used by Joran components.
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public abstract class JoranConstants {
	public static final String INCLUDED_TAG = "included";
	public static final String INCLUDE_TAG = "include";

    public static final String APPENDER_TAG = "appender";
    public static final String REF_ATTRIBUTE = "ref";
    public static final String ADDITIVITY_ATTRIBUTE = "additivity";
    public static final String LEVEL_ATTRIBUTE = "level";
    public static final String CONVERTER_CLASS_ATTRIBUTE = "converterClass";
    public static final String CONVERSION_WORD_ATTRIBUTE = "conversionWord";
    public static final String PATTERN_ATTRIBUTE = "pattern";
    public static final String VALUE_ATTR = "value";
    public static final String ACTION_CLASS_ATTRIBUTE = "actionClass";

    public static final String INHERITED = "INHERITED";
    public static final String NULL = "NULL";
    static final Class<?>[] ONE_STRING_PARAM = new Class[] { String.class };

    public static final String APPENDER_BAG = "APPENDER_BAG";
    public static final String APPENDER_REF_BAG = "APPENDER_REF_BAG";
    //public static final String FILTER_CHAIN_BAG = "FILTER_CHAIN_BAG";
}
