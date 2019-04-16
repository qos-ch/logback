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
import org.xml.sax.Locator;

import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 *
 * Most of the work for configuring logback is done by Actions.
 *
 * <p>Action methods are invoked as the XML file is parsed.
 *
 * <p>This class is largely inspired from the relevant class in the
 * commons-digester project of the Apache Software Foundation.
 *
 * @author Craig McClanahan
 * @author Christopher Lenz
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public abstract class Action extends ContextAwareBase {

    public static final String NAME_ATTRIBUTE = "name";
    public static final String KEY_ATTRIBUTE = "key";
    public static final String VALUE_ATTRIBUTE = "value";
    public static final String FILE_ATTRIBUTE = "file";
    public static final String CLASS_ATTRIBUTE = "class";
    public static final String PATTERN_ATTRIBUTE = "pattern";
    public static final String SCOPE_ATTRIBUTE = "scope";

    public static final String ACTION_CLASS_ATTRIBUTE = "actionClass";

    /**
     * Called when the parser encounters an element matching a
     * {@link ch.qos.logback.core.joran.spi.ElementSelector Pattern}.
     */
    public abstract void begin(InterpretationContext intercon, String name, Attributes attributes) throws ActionException;

    /**
     * Called to pass the body (as text) contained within an element.
     * @param ic
     * @param body
     * @throws ActionException
     */
    public void body(InterpretationContext intercon, String body) throws ActionException {
        // NOP
    }

    /*
     * Called when the parser encounters an endElement event matching a {@link ch.qos.logback.core.joran.spi.Pattern
     * Pattern}.
     */
    public abstract void end(InterpretationContext intercon, String name) throws ActionException;

    public String toString() {
        return this.getClass().getName();
    }

    protected int getColumnNumber(InterpretationContext intercon) {
        SaxEventInterpreter interpreter = intercon.getSaxEventInterpreter();
        Locator locator = interpreter.getLocator();
        if (locator != null) {
            return locator.getColumnNumber();
        }
        return -1;
    }

    // move to InterpretationContext
    static public int getLineNumber(InterpretationContext intercon) {
        SaxEventInterpreter interpreter = intercon.getSaxEventInterpreter();
        if(interpreter == null)
            return -1;
        Locator locator = interpreter.getLocator();
        if (locator != null) {
            return locator.getLineNumber();
        }
        return -1;
    }

    protected String getLineColStr(InterpretationContext intercon) {
        return "line: " + getLineNumber(intercon) + ", column: " + getColumnNumber(intercon);
    }
    
    protected String atLine(InterpretationContext intercon) {
    	return "At line "+intercon.getLineNumber();
    }
    
    protected String nearLine(InterpretationContext intercon) {
    	return "Near line "+intercon.getLineNumber();
    }
}
