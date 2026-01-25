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

package ch.qos.logback.core.net;

public class Innocent implements java.io.Serializable {

    private static final long serialVersionUID = -1227008349289885025L;

    int anInt;
    Integer anInteger;
    String aString;

    public int getAnInt() {
        return anInt;
    }

    public void setAnInt(int anInt) {
        this.anInt = anInt;
    }

    public Integer getAnInteger() {
        return anInteger;
    }

    public void setAnInteger(Integer anInteger) {
        this.anInteger = anInteger;
    }

    public String getaString() {
        return aString;
    }

    public void setaString(String aString) {
        this.aString = aString;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aString == null) ? 0 : aString.hashCode());
        result = prime * result + anInt;
        result = prime * result + ((anInteger == null) ? 0 : anInteger.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Innocent other = (Innocent) obj;
        if (aString == null) {
            if (other.aString != null)
                return false;
        } else if (!aString.equals(other.aString))
            return false;
        if (anInt != other.anInt)
            return false;
        if (anInteger == null) {
            if (other.anInteger != null)
                return false;
        } else if (!anInteger.equals(other.anInteger))
            return false;
        return true;
    }

}
