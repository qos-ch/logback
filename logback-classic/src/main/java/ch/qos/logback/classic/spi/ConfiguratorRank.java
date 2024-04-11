/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2023, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfiguratorRank {

    static public int FALLBACK = -10;
    static public int NOMINAL = 0;
    static public int SERIALIZED_MODEL = 10;

    static public int DEFAULT = 20;

    static public int CUSTOM_LOW_PRIORITY = DEFAULT;

    static public int CUSTOM_NORMAL_PRIORITY = 30;

    static public int CUSTOM_HIGH_PRIORITY = 40;

    static public int CUSTOM_TOP_PRIORITY = 50;
    public int value() default DEFAULT;
}
