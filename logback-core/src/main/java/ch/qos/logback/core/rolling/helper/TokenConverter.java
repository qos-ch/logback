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
package ch.qos.logback.core.rolling.helper;

/**
 * <code>TokenConverter</code> offers some basic functionality used by more 
 * specific token  converters. 
 * <p>
 * It basically sets up the chained architecture for tokens. It also forces 
 * derived classes to fix their type.
 * 
 * @author Ceki
 * @since 1.3
 */
public class TokenConverter {

    static final int IDENTITY = 0;
    static final int INTEGER = 1;
    static final int DATE = 1;
    int type;
    TokenConverter next;

    protected TokenConverter(int t) {
        type = t;
    }

    public TokenConverter getNext() {
        return next;
    }

    public void setNext(TokenConverter next) {
        this.next = next;
    }

    public int getType() {
        return type;
    }

    public void setType(int i) {
        type = i;
    }

}
