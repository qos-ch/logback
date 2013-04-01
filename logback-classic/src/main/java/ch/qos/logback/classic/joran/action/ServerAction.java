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

import ch.qos.logback.classic.net.server.SocketServer;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

/**
 * A Joran {@link Action} for a server configuration.
 *
 * @author Carl Harris
 */
public class ServerAction extends Action {

  private SocketServer server;
  private boolean inError;
  
  @Override
  public void begin(InterpretationContext ic, String name,
      Attributes attributes) throws ActionException {
    String className = attributes.getValue(CLASS_ATTRIBUTE);
    if (OptionHelper.isEmpty(className)) {
      className = SocketServer.class.getCanonicalName();
    }
    try {
      addInfo("About to instantiate server of type [" + className + "]");

      server = (SocketServer) OptionHelper.instantiateByClassName(className,
          SocketServer.class, context);
      server.setContext(context);
      ic.pushObject(server);
    }
    catch (Exception ex) {
      inError = true;
      addError("Could not create a server of type [" + className + "].", ex);
      throw new ActionException(ex);
    }
  }

  @Override
  public void end(InterpretationContext ic, String name)
      throws ActionException {
    
    if (inError) return;
    
    server.start();

    Object o = ic.peekObject();
    if (o != server) {
      addWarn("The object at the of the stack is not the server " +
      		"pushed earlier.");
    } else {
      ic.popObject();
    }
  }

}
