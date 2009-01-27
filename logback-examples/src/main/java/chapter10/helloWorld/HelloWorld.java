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

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.util.StatusPrinter;
import chapter10.SimpleConfigurator;

/**
 * 
 * A hello world example using Joran.
 * 
 * @author Ceki Gulcu
 */
public class HelloWorld {
  public static void main(String[] args) throws Exception {
    Map<Pattern, Action> ruleMap = new HashMap<Pattern, Action>();

    // Associate "hello-world" pattern with HelloWorldAction
    ruleMap.put(new Pattern("hello-world"), new HelloWorldAction());

    // Joran needs to work within a context.
    Context context = new ContextBase();
    SimpleConfigurator simpleConfigurator = new SimpleConfigurator(ruleMap);
    // link the configurator with its context
    simpleConfigurator.setContext(context);

    simpleConfigurator.doConfigure(args[0]);
    StatusPrinter.print(context);
  }
}
