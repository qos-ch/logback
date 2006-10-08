/**
 * Logback: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package joran.calculator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SimpleRuleStore;
import ch.qos.logback.core.util.StatusPrinter;


/**
 * This examples illustrates collaboration between multiple actions through the
 * common execution context stack.
 * 
 * The first and only argument of this application must be the path to
 * the XML file to interpret. There are sample XML files in the 
 * <em>examples/src/joran/calculator/</em> directory. 
 *
 * For example,
 *
<pre>
    java joran.calculator.Calculator1 examples/src/joran/calculator/calculator1.xml
</pre>
 *
 * Please refer to the comments in the source code for more information.
 * 
 * @author Ceki G&uuml;ulc&uuml;
 */
public class Calculator1 {
  
  
  public static void main(String[] args) throws Exception {
    Context context = new ContextBase();
    
    // Create a simple rule store where pattern and action associations will
    // be kept. This is a basic requirement before invoking a Joran Interpreter.
    RuleStore ruleStore = new SimpleRuleStore(context);

    // Associate "/computation" pattern with  ComputationAction1
    ruleStore.addRule(new Pattern("/computation"), new ComputationAction1());

    // Other associations
    ruleStore.addRule(new Pattern("/computation/literal"), new LiteralAction());
    ruleStore.addRule(new Pattern("/computation/add"), new AddAction());
    ruleStore.addRule(new Pattern("/computation/multiply"), new MultiplyAction());
    
    // Create a new Joran Interpreter and hand it our simple rule store.
    Interpreter ji = new Interpreter(ruleStore);

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
