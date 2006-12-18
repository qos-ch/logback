/**
 * Logback: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package joran.helloWorld;

import java.util.HashMap;
import java.util.Map;

import joran.SimpleConfigurator;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.util.StatusPrinter;


/**
 *
 * A hello world example using Joran.
 *
 * The first and only argument of this application must be the path to
 * the XML file to interpret.
 *
 * For example,
 *
<pre>
    java joran.helloWorld.HelloWorld examples/src/joran/helloWorld/hello.xml
</pre>
 *
 * @author Ceki Gulcu
 */
public class HelloWorld {
  public static void main(String[] args) throws Exception {
    Map<Pattern, Action> ruleMap = new HashMap<Pattern, Action>();

    // Associate "hello-world" pattern with  HelloWorldAction
    ruleMap.put(new Pattern("hello-world"), new HelloWorldAction());

    // Joran needs to work within a context.
    Context context = new ContextBase();
    SimpleConfigurator simpleConfigurator = new SimpleConfigurator(ruleMap);
    // link the configurator with its context
    simpleConfigurator.setContext(context);

    try {
      simpleConfigurator.doConfigure(args[0]);
    } catch (JoranException e) {
      // Print any errors that might have occured.
      StatusPrinter.print(context);
    }
      }
}
