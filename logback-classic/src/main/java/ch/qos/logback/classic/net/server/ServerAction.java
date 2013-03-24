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
package ch.qos.logback.classic.net.server;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;

/**
 * A simple Joran {@link Action} for the server configuration.
 * <p>
 * The purpose of this action is simply to put a {@link ServerConfiguration} 
 * on the top of the interpreter's stack.
 *
 * @author Carl Harris
 */
public class ServerAction extends Action {

  private final ServerConfiguration config;
  
  public ServerAction(ServerConfiguration config) {
    this.config = config;    
  }
  
  @Override
  public void begin(InterpretationContext ic, String name,
      Attributes attributes) throws ActionException {
    ic.pushObject(config);
  }

  @Override
  public void end(InterpretationContext ic, String name)
      throws ActionException {
  }

}
