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
package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.spi.BasicSequenceNumberGenerator;
import ch.qos.logback.core.spi.SequenceNumberGenerator;
import ch.qos.logback.core.util.OptionHelper;

/**
 * Action which handles &lt;sequenceNumberGenerator&gt; elements in configuration files.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class SequenceNumberGeneratorAction extends Action {

    SequenceNumberGenerator sequenceNumberGenerator;
    private boolean inError;

    /**
     * Instantiates a shutdown hook of the given class and sets its name.
     * 
     * The hook thus generated is placed in the {@link InterpretationContext}'s
     * shutdown hook bag.
     */
    @Override
    public void begin(InterpretationContext ic, String name, Attributes attributes) throws ActionException {
        sequenceNumberGenerator = null;
        inError = false;

        String className = attributes.getValue(CLASS_ATTRIBUTE);
        if (OptionHelper.isEmpty(className)) {
            className = BasicSequenceNumberGenerator.class.getName();
            addInfo("Assuming className [" + className + "]");
        }

        try {
            addInfo("About to instantiate SequenceNumberGenerator of type [" + className + "]");

            sequenceNumberGenerator = (SequenceNumberGenerator) OptionHelper.instantiateByClassName(className, SequenceNumberGenerator.class, context);
            sequenceNumberGenerator.setContext(context);

            ic.pushObject(sequenceNumberGenerator);
        } catch (Exception e) {
            inError = true;
            addError("Could not create a SequenceNumberGenerator of type [" + className + "].", e);
            throw new ActionException(e);
        }
    }

    /**
     * Once the children elements are also parsed, now is the time to activate the
     * shutdown hook options.
     */
    @Override
    public void end(InterpretationContext ic, String name) throws ActionException {
        if (inError) {
            return;
        }

        Object o = ic.peekObject();
        if (o != sequenceNumberGenerator) {
            addWarn("The object at the of the stack is not the hook pushed earlier.");
        } else {
            ic.popObject();

            addInfo("Registering sequenceNumberGenerator with context.");
            context.setSequenceNumberGenerator(sequenceNumberGenerator);
        }
    }
}
