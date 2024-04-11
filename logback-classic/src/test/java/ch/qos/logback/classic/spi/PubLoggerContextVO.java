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

import ch.qos.logback.classic.LoggerContext;

import java.util.Map;
import java.util.Objects;

/**
 * PubLoggerContextVO is a very open (public) version of LoggerContextVO
 *
 * @since 1.4.8
 */
public class PubLoggerContextVO extends LoggerContextVO {


    public PubLoggerContextVO(LoggerContext lc) {
       super(lc);
    }

    public PubLoggerContextVO(String name, Map<String, String> propertyMap, long birthTime) {
        super(name, propertyMap, birthTime);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPropertyMap(Map<String, String> propertyMap) {
        this.propertyMap = propertyMap;
    }


    public void setBirthTime(long birthTime) {
        this.birthTime = birthTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PubLoggerContextVO that = (PubLoggerContextVO) o;
        return birthTime == that.birthTime && Objects.equals(name, that.name) && Objects.equals(propertyMap,
                that.propertyMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, propertyMap, birthTime);
    }
}
