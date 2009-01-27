/**
 * Logback: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package chapter10.helloWorld;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;

/**
 * A trivial action that writes "Hello world" on the console.
 * 
 * See the {@link HelloWorld} class for integration with Joran.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class HelloWorldAction extends Action {
  public void begin(InterpretationContext ec, String name, Attributes attributes) {
    System.out.println("Hello World");
  }

  public void end(InterpretationContext ec, String name) {
  }
}
