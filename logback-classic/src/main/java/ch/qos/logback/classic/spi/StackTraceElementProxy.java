/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.spi;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.core.CoreConstants;

import java.io.Serializable;
import java.util.Objects;

import static ch.qos.logback.classic.ClassicConstants.DECLARING_CLASS_NA;
import static ch.qos.logback.classic.ClassicConstants.FILENAME_NA;
import static ch.qos.logback.classic.ClassicConstants.LINE_NUMBER_NA;
import static ch.qos.logback.classic.ClassicConstants.METHOD_NAME_NA;

public class StackTraceElementProxy implements Serializable {

    private static final long serialVersionUID = -2374374378980555982L;

    final StackTraceElement ste;
    // save a byte or two during serialization, as we can
    // reconstruct this field from 'ste'
    transient private String steAsString;

    @Deprecated
    ClassPackagingData classPackagingData;

    // See https://github.com/qos-ch/logback/issues/1040
    static final StackTraceElement NA_SUBSTITUTE = new StackTraceElement(DECLARING_CLASS_NA, METHOD_NAME_NA,
                                                                        FILENAME_NA, LINE_NUMBER_NA);

    public StackTraceElementProxy(StackTraceElement ste) {
        // while null StackTraceElement is not expected, we defensively replace it with NA_SUBSTITUTE.
        this.ste = Objects.requireNonNullElse(ste, NA_SUBSTITUTE);
    }

    public String getSTEAsString() {
        if (steAsString == null) {
            steAsString = "at " + ste.toString();
        }
        return steAsString;
    }

    public StackTraceElement getStackTraceElement() {
        return ste;
    }

    public void setClassPackagingData(ClassPackagingData cpd) {
        if (this.classPackagingData != null) {
            throw new IllegalStateException("Packaging data has been already set");
        }
        this.classPackagingData = cpd;
    }

    public ClassPackagingData getClassPackagingData() {
        return classPackagingData;
    }

    @Override
    public int hashCode() {
        return ste.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final StackTraceElementProxy other = (StackTraceElementProxy) obj;

        if (!ste.equals(other.ste)) {
            return false;
        }
        if (classPackagingData == null) {
            if (other.classPackagingData != null) {
                return false;
            }
        } else if (!classPackagingData.equals(other.classPackagingData)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getSTEAsString();
    }
}
