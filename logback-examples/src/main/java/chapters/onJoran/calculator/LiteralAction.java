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
 * This action converts the value attribute of the associated element to an
 * integer and pushes the resulting Integer object on top of the execution
 * context stack.
 * 
 * <p>It also illustrates usage of Joran's error reporting/handling paradigm.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class LiteralAction extends Action {
    public static final String VALUE_ATR = "value";

    public void begin(InterpretationContext ic, String name, Attributes attributes) {
        String valueStr = attributes.getValue(VALUE_ATR);

        if (OptionHelper.isNullOrEmpty(valueStr)) {
            ic.addError("The literal action requires a value attribute");
            return;
        }

        try {
            Integer i = Integer.valueOf(valueStr);
            ic.pushObject(i);
        } catch (NumberFormatException nfe) {
            ic.addError("The value [" + valueStr + "] could not be converted to an Integer", nfe);
            throw nfe;
        }
    }

    public void end(InterpretationContext ic, String name) {
        // Nothing to do here.
        // In general, the end() method of actions associated with elements
        // having no children do not need to perform any processing in their
        // end() method.
    }
}
