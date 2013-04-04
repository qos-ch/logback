/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.classic.net.SocketRemote;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

/**
 * A Joran {@link Action} for a {@link SocketRemote} configuration.
 *
 * @author Carl Harris
 */
public class SocketRemoteAction extends Action {

  private SocketRemote remote;
  private boolean inError;
  
  @Override
  public void begin(InterpretationContext ic, String name,
      Attributes attributes) throws ActionException {
    String className = attributes.getValue(CLASS_ATTRIBUTE);
    if (OptionHelper.isEmpty(className)) {
      className = SocketRemote.class.getCanonicalName();
    }

    try {
      addInfo("About to instantiate remote of type [" + className + "]");

      remote = (SocketRemote) OptionHelper.instantiateByClassName(
          className, SocketRemote.class, context);
      remote.setContext(context);

      ic.pushObject(remote);
    }
    catch (Exception ex) {
      inError = true;
      addError("Could not create a remote of type [" + className + "].", ex);
      throw new ActionException(ex);
    }
  }

  @Override
  public void end(InterpretationContext ic, String name)
      throws ActionException {
    
    if (inError) return;
    
    remote.start();

    Object o = ic.peekObject();
    if (o != remote) {
      addWarn("The object at the of the stack is not the remote " +
      		"pushed earlier.");
    } else {
      ic.popObject();
    }
  }

}
