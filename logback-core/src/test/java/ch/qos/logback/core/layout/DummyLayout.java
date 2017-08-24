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
package ch.qos.logback.core.layout;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;

public class DummyLayout<E> extends LayoutBase<E> {

    public static final String DUMMY = "dummy" + CoreConstants.LINE_SEPARATOR;
    String val = DUMMY;

    public DummyLayout() {
    }

    public DummyLayout(String val) {
        this.val = val;
    }

    public String doLayout(E event) {
        return val;
    }

}
