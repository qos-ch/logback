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

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

/**
 * ComputationAction1 will print the result of the compuration made by 
 * children elements but only if the compuration itself is named, that is if the
 * name attribute of the associated computation element is not null. In other
 * words, anonymous computations will not print their result.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class ComputationAction1 extends Action {
    public static final String NAME_ATR = "name";

    String nameStr;

    /**
     * Store the value of the name attribute for future use.
     */
    public void begin(InterpretationContext ec, String name, Attributes attributes) {
        nameStr = attributes.getValue(NAME_ATR);
    }

    /**
     * Children elements have been processed. The sesults should be an integer 
     * placed at the top of the execution stack.
     * 
     * This value will be printed on the console but only if the action is 
     * named. Anonymous computation will not print their result.
     */
    public void end(InterpretationContext ec, String name) {
        if (OptionHelper.isNullOrEmpty(nameStr)) {
            // nothing to do
        } else {
            Integer i = (Integer) ec.peekObject();
            System.out.println("The computation named [" + nameStr + "] resulted in the value " + i);
        }
    }
}
