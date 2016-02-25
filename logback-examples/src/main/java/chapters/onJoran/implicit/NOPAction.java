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
package chapters.onJoran.implicit;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;

/**
 * No operation (NOP) action that does strictly nothing. 
 *  
 * @author Ceki G&uuml;lc&uuml;
 */
public class NOPAction extends Action {

    public void begin(InterpretationContext ec, String name, Attributes attributes) {
    }

    public void end(InterpretationContext ec, String name) {
    }
}
