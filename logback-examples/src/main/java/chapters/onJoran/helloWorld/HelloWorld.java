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
package chapters.onJoran.helloWorld;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.util.StatusPrinter;
import chapters.onJoran.SimpleConfigurator;

/**
 * 
 * A hello world example using Joran.
 * 
 * @author Ceki Gulcu
 */
public class HelloWorld {
    public static void main(String[] args) throws Exception {
        Map<ElementSelector, Action> ruleMap = new HashMap<ElementSelector, Action>();

        // Associate "hello-world" pattern with HelloWorldAction
        ruleMap.put(new ElementSelector("hello-world"), new HelloWorldAction());

        // Joran needs to work within a context.
        Context context = new ContextBase();
        SimpleConfigurator simpleConfigurator = new SimpleConfigurator(ruleMap);
        // link the configurator with its context
        simpleConfigurator.setContext(context);

        simpleConfigurator.doConfigure(args[0]);
        StatusPrinter.print(context);
    }
}
