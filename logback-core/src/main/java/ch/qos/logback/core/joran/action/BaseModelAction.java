/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;

public abstract class BaseModelAction extends Action {

    Model parentModel;
    Model currentModel;
    boolean inError = false;

    @Override
    public void begin(SaxEventInterpretationContext intercon, String name, Attributes attributes)
            throws ActionException {
        parentModel = null;
        inError = false;

        if (!validPreconditions(intercon, name, attributes)) {
            inError = true;
            return;
        }

        currentModel = buildCurrentModel(intercon, name, attributes);
        currentModel.setTag(name);
        if (!intercon.isModelStackEmpty()) {
            parentModel = intercon.peekModel();
        }
        final int lineNumber = getLineNumber(intercon);
        currentModel.setLineNumber(lineNumber);
        intercon.pushModel(currentModel);
    }

    abstract protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name,
            Attributes attributes);

    /**
     * Validate preconditions of this action.
     * 
     * By default, true is returned. Sub-classes should override appropriatelly.
     * 
     * @param interpretationContext
     * @param name
     * @param attributes
     * @return
     */
    protected boolean validPreconditions(SaxEventInterpretationContext intercon, String name, Attributes attributes) {
        return true;
    }

    @Override
    public void body(SaxEventInterpretationContext ec, String body) throws ActionException {
        if(currentModel == null) {
            throw new ActionException("current model is null. Is <configuration> element missing?");
        }
        currentModel.addText(body);
    }

    @Override
    public void end(SaxEventInterpretationContext interpretationContext, String name) throws ActionException {
        if (inError)
            return;

        Model m = interpretationContext.peekModel();

        if (m != currentModel) {
            addWarn("The object at the of the stack is not the model [" + currentModel.idString()
                    + "] pushed earlier.");
            addWarn("This is wholly unexpected.");
        }

        // do not pop nor add to parent if there is no parent
        if (parentModel != null) {
            parentModel.addSubModel(currentModel);
            interpretationContext.popModel();
        }
    }
}
