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
package ch.qos.logback.classic.spi.special;

import ch.qos.logback.classic.spi.CPDCSpecial;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.PackagingDataCalculator;
import ch.qos.logback.classic.spi.ThrowableProxy;

public class CPDCSpecialImpl implements CPDCSpecial {

    Throwable throwable;
    IThrowableProxy throwableProxy;

    public void doTest() {
        nesting();
    }

    private void nesting() {
        throwable = new Throwable("x");
        throwableProxy = new ThrowableProxy(throwable);
        PackagingDataCalculator pdc = new PackagingDataCalculator();
        pdc.calculate(throwableProxy);
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public IThrowableProxy getThrowableProxy() {
        return throwableProxy;
    }
}
