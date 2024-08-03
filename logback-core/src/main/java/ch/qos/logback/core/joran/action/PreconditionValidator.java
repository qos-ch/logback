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
    SaxEventInterpretationContext seic;
    Attributes attributes;
    String tag;

    public PreconditionValidator(ContextAware origin, SaxEventInterpretationContext seic, String name,
            Attributes attributes) {
        super(origin);
        this.setContext(origin.getContext());
        this.seic = seic;
        this.tag = name;
        this.attributes = attributes;
    }

    public PreconditionValidator validateZeroAttributes() {
        if(attributes == null) 
            return this;
        
        if(attributes.getLength() != 0) {
            addError("Element [" + tag + "] should have no attributes, near line "
                    + Action.getLineNumber(seic));
            this.valid = false;
        }
        return this;
    }

    
    public PreconditionValidator validateClassAttribute() {
        return validateGivenAttribute(Action.CLASS_ATTRIBUTE);
    }

    public PreconditionValidator validateNameAttribute() {
        return validateGivenAttribute(Action.NAME_ATTRIBUTE);
    }

    public PreconditionValidator validateValueAttribute() {
        return validateGivenAttribute(JoranConstants.VALUE_ATTR);
    }

    public PreconditionValidator validateRefAttribute() {
        return validateGivenAttribute(JoranConstants.REF_ATTRIBUTE);
    }

    public boolean isInvalidAttribute(String attributeName) {
        String attributeValue = attributes.getValue(attributeName);
        return OptionHelper.isNullOrEmptyOrAllSpaces(attributeValue);
    }

    public PreconditionValidator validateGivenAttribute(String attributeName) {
        boolean invalid = isInvalidAttribute(attributeName);
        if (invalid) {
            addMissingAttributeError(attributeName);
            this.valid = false;
        }
        return this;
    }



    /**
     *
     * @deprecated replaced by {@link #validateGivenAttribute(String)}
     */
    @Deprecated
    public PreconditionValidator generic(String attributeName) {
        return validateGivenAttribute(attributeName);
    }

    public void addMissingAttributeError(String attributeName) {
        addError("Missing attribute [" + attributeName + "]. " + getLocationSuffix());
    }

    public String getLocationSuffix() {
        return "See element [" + tag + "] near line " + Action.getLineNumber(seic);
    }

//    public void addWarning(String msg) {
//        super.addWarn(msg + getLocationSuffix());
//    }
//
//    public void addError(String msg) {
//        super.addError(msg + getLocationSuffix());
//    }

    public boolean isValid() {
        return valid;
    }

}
