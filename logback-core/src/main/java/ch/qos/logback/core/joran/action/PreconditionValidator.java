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

import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.OptionHelper;

public class PreconditionValidator extends ContextAwareBase {

    boolean valid = true;
    SaxEventInterpretationContext intercon;
    Attributes attributes;
    String tag;

    public PreconditionValidator(ContextAware origin, SaxEventInterpretationContext intercon, String name,
            Attributes attributes) {
        super(origin);
        this.setContext(origin.getContext());
        this.intercon = intercon;
        this.tag = name;
        this.attributes = attributes;
    }

    public PreconditionValidator validateZeroAttributes() {
        if(attributes == null) 
            return this;
        
        if(attributes.getLength() != 0) {
            addError("Element [" + tag + "] should have no attributes, near line "
                    + Action.getLineNumber(intercon));
            this.valid = false;
        }
        return this;
    }

    
    public PreconditionValidator validateClassAttribute() {
        return generic(Action.CLASS_ATTRIBUTE);
    }

    public PreconditionValidator validateNameAttribute() {
        return generic(Action.NAME_ATTRIBUTE);
    }

    public PreconditionValidator validateValueAttribute() {
        return generic(JoranConstants.VALUE_ATTR);
    }

    public PreconditionValidator validateRefAttribute() {
        return generic(JoranConstants.REF_ATTRIBUTE);
    }

    public PreconditionValidator generic(String attributeName) {
        String attributeValue = attributes.getValue(attributeName);
        if (OptionHelper.isNullOrEmpty(attributeValue)) {
            addError("Missing attribute [" + attributeName + "] in element [" + tag + "] near line "
                    + Action.getLineNumber(intercon));
            this.valid = false;
        }
        return this;
    }

    public boolean isValid() {
        return valid;
    }

}
