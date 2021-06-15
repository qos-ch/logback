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
package ch.qos.logback.classic.spi;

import ch.qos.logback.core.CoreConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class ThrowableProxy implements IThrowableProxy {

    private Throwable throwable;
    private String className;
    private String message;
    // package-private because of ThrowableProxyUtil
    StackTraceElementProxy[] stackTraceElementProxyArray;
    // package-private because of ThrowableProxyUtil
    int commonFrames;
    private ThrowableProxy cause;
    private ThrowableProxy[] suppressed = NO_SUPPRESSED;
    private final Set<Throwable> alreadyProcessedSet;

    private transient PackagingDataCalculator packagingDataCalculator;
    private boolean calculatedPackageData = false;

    private static final ThrowableProxy[] NO_SUPPRESSED = new ThrowableProxy[0];

    public ThrowableProxy(Throwable throwable) {
        // Using Collections.newSetFromMap(new IdentityHashMap<>()) is inspired from
        // Throwable.printStackTrace(PrintStreamOrWriter):
        // Guard against malicious overrides of Throwable.equals by
        // using a Set with identity equality semantics.
        this(throwable, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    public ThrowableProxy(Throwable throwable, Set<Throwable> alreadyProcessedSet) {

        this.throwable = throwable;
        this.className = throwable.getClass().getName();
        this.message = throwable.getMessage();
        this.stackTraceElementProxyArray = ThrowableProxyUtil.steArrayToStepArray(throwable.getStackTrace());
        this.alreadyProcessedSet = alreadyProcessedSet;

        alreadyProcessedSet.add(throwable);

        Throwable nested = throwable.getCause();

        if (nested != null && !alreadyProcessedSet.contains(nested)) {
            this.cause = new ThrowableProxy(nested, alreadyProcessedSet);
            this.cause.commonFrames = ThrowableProxyUtil.findNumberOfCommonFrames(nested.getStackTrace(), stackTraceElementProxyArray);
        }
        Throwable[] throwableSuppressed = throwable.getSuppressed();
        if (throwableSuppressed.length > 0) {
            List<ThrowableProxy> suppressedList = new ArrayList<>(throwableSuppressed.length);
            for (int i = 0; i < throwableSuppressed.length; i++) {
                Throwable sup = throwableSuppressed[i];
                if (alreadyProcessedSet.contains(sup)) {
                    continue; // loop detected
                }
                ThrowableProxy throwableProxy = new ThrowableProxy(sup, alreadyProcessedSet);
                throwableProxy.commonFrames = ThrowableProxyUtil.findNumberOfCommonFrames(sup.getStackTrace(),
                        stackTraceElementProxyArray);
                suppressedList.add(throwableProxy);
            }
            this.suppressed = suppressedList.toArray(new ThrowableProxy[suppressedList.size()]);
        }
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public String getMessage() {
        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.qos.logback.classic.spi.IThrowableProxy#getClassName()
     */
    public String getClassName() {
        return className;
    }

    public StackTraceElementProxy[] getStackTraceElementProxyArray() {
        return stackTraceElementProxyArray;
    }

    public int getCommonFrames() {
        return commonFrames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.qos.logback.classic.spi.IThrowableProxy#getCause()
     */
    public IThrowableProxy getCause() {
        return cause;
    }

    public IThrowableProxy[] getSuppressed() {
        return suppressed;
    }

    public PackagingDataCalculator getPackagingDataCalculator() {
        // if original instance (non-deserialized), and packagingDataCalculator
        // is not already initialized, then create an instance.
        // here we assume that (throwable == null) for deserialized instances
        if (throwable != null && packagingDataCalculator == null) {
            packagingDataCalculator = new PackagingDataCalculator();
        }
        return packagingDataCalculator;
    }

    public void calculatePackagingData() {
        if (calculatedPackageData) {
            return;
        }
        PackagingDataCalculator pdc = this.getPackagingDataCalculator();
        if (pdc != null) {
            calculatedPackageData = true;
            pdc.calculate(this);
        }
    }

    public void fullDump() {
        StringBuilder builder = new StringBuilder();
        for (StackTraceElementProxy step : stackTraceElementProxyArray) {
            String string = step.toString();
            builder.append(CoreConstants.TAB).append(string);
            ThrowableProxyUtil.subjoinPackagingData(builder, step);
            builder.append(CoreConstants.LINE_SEPARATOR);
        }
        System.out.println(builder.toString());
    }

}
