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

import ch.qos.logback.core.joran.spi.ElementPath;
import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.InterpretationContext;

/**
 * ImplcitActions are like normal (explicit) actions except that are applied
 * by the parser when no other pattern applies. Since there can be many implicit
 * actions, each action is asked whether it applies in the given context. The
 * first implicit action to respond positively is then applied. See also the
 * {@link #isApplicable} method.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public abstract class ImplicitAction extends Action {

    /**
     * Check whether this implicit action is appropriate in the current context.
     * 
     * @param currentElementPath This pattern contains the tag name of the current
     * element being parsed at the top of the stack.
     * @param attributes The attributes of the current element to process.
     * @param ec
     * @return Whether the implicit action is applicable in the current context
     */
    public abstract boolean isApplicable(ElementPath currentElementPath, Attributes attributes, InterpretationContext ec);

}
