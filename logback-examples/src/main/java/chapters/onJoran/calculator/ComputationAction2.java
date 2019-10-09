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

import java.util.Stack;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

/**
 * ComputationAction2 will print the result of the compuration made by 
 * children elements but only if the computation itself is named, that is if the
 * name attribute of the associated computation element is not null. In other
 * words, anonymous computations will not print their result.
 * 
 * ComputationAction2 differs from ComputationAction1 in its handling of
 * instance variables. ComputationAction1 has a simple <Code>nameStr</code>
 * instance variable. This variable is set when the begin() method is called 
 * and then later used within the end() method. 
 * 
 * This simple approach works properly if the begin() and end()
 * method of a given action are expected to be called in sequence. However,
 * there are situations where the begin() method of the same action instance is 
 * invoked multiple times before the matching end() method is invoked. 
 * 
 * When this happens, the second call to begin() overwrites values set by
 * the first invocation to begin(). The solution is to save parameter values 
 * into a separate stack. The well-formedness of XML will guarantee that a value
 * saved by one begin() will be consumed only by the matching end() method.
 * 
 * Note that in the vast majority of cases there is no need to resort to a 
 * separate stack for each variable. The situation of successive begin() 
 * invocations can only occur if: 
 * 
 * 1) the associated pattern contains a wildcard, i.e. the &#42; character
 * 
 * and
 * 
 * 2) the associated element tag can contain itself as a child 
 *  
 * For example, "&#42;/computation" pattern means that computations can contain
 * other computation elements as children. 
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class ComputationAction2 extends Action {
    public static final String NAME_ATR = "name";

    Stack<String> nameStrStack = new Stack<String>();

    public void begin(InterpretationContext ec, String name, Attributes attributes) {
        String nameStr = attributes.getValue(NAME_ATR);
        // save nameStr value in a special stack. Note that the value is saved
        // even if it is empty or null.
        nameStrStack.push(nameStr);
    }

    public void end(InterpretationContext ec, String name) {
        // pop nameStr value from the special stack
        String nameStr = (String) nameStrStack.pop();

        if (OptionHelper.isNullOrEmpty(nameStr)) {
            // nothing to do
        } else {
            Integer i = (Integer) ec.peekObject();
            System.out.println("The computation named [" + nameStr + "] resulted in the value " + i);
        }
    }
}
