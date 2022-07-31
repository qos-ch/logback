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

import java.util.Objects;

public class TimestampModel extends NamedModel {

    private static final long serialVersionUID = 2096655273673863306L;

    public static final String CONTEXT_BIRTH = "contextBirth";

    String datePattern;
    String timeReference;
    String scopeStr;

    @Override
    protected TimestampModel makeNewInstance() {
        return new TimestampModel();
    }
    
    @Override
    protected void mirror(Model that) {
        TimestampModel actual = (TimestampModel) that;
        super.mirror(actual);
        this.datePattern = actual.datePattern;
        this.timeReference = actual.timeReference;
        this.scopeStr = actual.scopeStr;
    }
    
    public String getKey() {
        return getName();
    }

    public void setKey(String key) {
        this.setName(key);
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public String getTimeReference() {
        return timeReference;
    }

    public void setTimeReference(String timeReference) {
        this.timeReference = timeReference;
    }

    public String getScopeStr() {
        return scopeStr;
    }

    public void setScopeStr(String scopeStr) {
        this.scopeStr = scopeStr;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(datePattern, scopeStr, timeReference);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        TimestampModel other = (TimestampModel) obj;
        return Objects.equals(datePattern, other.datePattern) && Objects.equals(scopeStr, other.scopeStr)
                && Objects.equals(timeReference, other.timeReference);
    }

    
}
