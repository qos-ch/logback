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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.ExecutionContext;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SimpleRuleStore;
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
    // Create a simple rule store where pattern and action associations will
    // be kept.
    Context context = new ContextBase();
    RuleStore ruleStore = new SimpleRuleStore(context);

    // Associate "hello-world" pattern with  HelloWorldAction
    ruleStore.addRule(new Pattern("hello-world"), new HelloWorldAction());

    // Create a new Joran Interpreter and hand it our simple rule store.
    Interpreter ji = new Interpreter(ruleStore);
    ExecutionContext ec = ji.getExecutionContext();
    ec.setContext(context);
    
    // Create a SAX parser
    SAXParserFactory spf = SAXParserFactory.newInstance();
    SAXParser saxParser = spf.newSAXParser();

    // Parse the file given as the application's first argument and
    // set the SAX ContentHandler to the Joran Interpreter we just created.
    saxParser.parse(args[0], ji);
    
    // The file has been parsed and interpreted. We now print any errors that 
    // might have occured.
    StatusPrinter.print(context);
  }
}
