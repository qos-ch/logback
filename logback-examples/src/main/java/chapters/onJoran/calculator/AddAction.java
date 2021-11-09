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
package chapters.onJoran.calculator;

import java.util.EmptyStackException;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;

/**
 * This action adds the two integers at the top of the stack (they are removed)
 * and pushes the result to the top the stack.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class AddAction extends Action {

    @Override
    public void begin(final InterpretationContext ic, final String name, final Attributes attributes) {
        final int first = fetchInteger(ic);
        final int second = fetchInteger(ic);
        // Push the result of the addition for the following actions.
        ic.pushObject(first + second);
    }

    /**
     * Pop the Integer object at the top of the stack.
     * This code also  illustrates usage of Joran's error handling paradigm.
     */
    int fetchInteger(final InterpretationContext ic) {
        int result = 0;

        try {
            // Pop the object at the top of the interpretation context's stack.
            final Object o1 = ic.popObject();

            if (!(o1 instanceof Integer)) {
                final String errMsg = "Object [" + o1 + "] currently at the top of the stack is not an integer.";
                ic.addError(errMsg);
                throw new IllegalArgumentException(errMsg);
            }
            result = ((Integer) o1);
        } catch (final EmptyStackException ese) {
            ic.addError("Expecting an integer on the execution stack.");
            throw ese;
        }
        return result;
    }

    @Override
    public void end(final InterpretationContext ic, final String name) {
        // Nothing to do here.
    }
}
