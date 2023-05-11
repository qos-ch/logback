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

package ch.qos.logback.classic.jsonTest;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;

import java.util.Arrays;
import java.util.Objects;

public class ThrowableProxyComparator {


    static public boolean areEqual(IThrowableProxy left, IThrowableProxy right) {

        if(left == right)
            return true;

        if(left == null)
            return false;

        if(!left.getClassName().equals(right.getClassName()))
            return false;



        if(!left.getMessage().equals(right.getMessage()))
            return false;


        StackTraceElementProxy[] leftStepArray = left.getStackTraceElementProxyArray();
        StackTraceElementProxy[] rightStepArray = right.getStackTraceElementProxyArray();

        System.out.println("before equalsSTEPArray");

        if(!equalsSTEPArray(leftStepArray, rightStepArray))
            return false;


        return true;
    }

    static public boolean equalsSTEPArray( StackTraceElementProxy[] leftStepArray,  StackTraceElementProxy[] rightStepArray) {
        if (leftStepArray==rightStepArray)
            return true;
        if (leftStepArray==null || rightStepArray==null)
            return false;

        int length = leftStepArray.length;
        if (rightStepArray.length != length) {
            System.out.println("length discrepancy");
            return false;
        }

        System.out.println("checking ste array elements");

        for (int i=0; i<length; i++) {
            StackTraceElementProxy leftStep = leftStepArray[i];
            StackTraceElementProxy rightStep = rightStepArray[i];

            if (!equalsSTEP(leftStep, rightStep)) {
                System.out.println("left "+leftStep);
                System.out.println("right "+rightStep);

                return false;
            }
        }
        return true;
    }

    static public boolean equalsSTEP(StackTraceElementProxy left, StackTraceElementProxy right) {
            if (left==right)
                return true;

        if (right == null)
            return false;

        StackTraceElement l = left.getStackTraceElement();
        StackTraceElement r = right.getStackTraceElement();

        if(!Objects.equals(l.getClassName(), (r.getClassName())))
            return false;


        if(!Objects.equals(l.getMethodName(), (r.getMethodName())))
            return false;


        if(!Objects.equals(l.getFileName(), (r.getFileName())))
            return false;

        if(l.getLineNumber() != r.getLineNumber())
            return false;


        return true;
    }
}
