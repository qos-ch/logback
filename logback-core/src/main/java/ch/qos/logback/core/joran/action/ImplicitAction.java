/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran.action;


import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.ExecutionContext;
import ch.qos.logback.core.joran.spi.Pattern;


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
   * @param currentPattern This pattern contains the tag name of the current 
   * element being parsed at the top of the stack.
   * @param attributes The attributes of the current element to process.
   * @param ec
   * @return Whether the implicit action is applicable in the current context
   */
  public abstract boolean isApplicable(
    Pattern currentPattern, Attributes attributes, ExecutionContext ec);
  
  
}
