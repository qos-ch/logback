/**
 * Logback: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */


package joran.implicit;

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
 * This example illustrates the usage of implcit actions.
 * 
 * The crucial point to remember about implicit actions is that they
 * are not associated with a pattern. Moreover, they are added directly to
 * a Joran Interpreter instead of a rule store.
 * 
 * @author Ceki G&uuml;ulc&uuml;
 */
public class PrintMe {
  
  
  public static void main(String[] args) throws Exception {
    Context context = new ContextBase();
    
    RuleStore ruleStore = new SimpleRuleStore(context);

    // we start with the rule for the top-most (root) element
    ruleStore.addRule(new Pattern("*/foo"), new NOPAction());


    // Create a new Joran Interpreter and hand it our simple rule store.
    Interpreter ji = new Interpreter(ruleStore);
    // set the context for the interpreter's execution context
    ExecutionContext ec = ji.getExecutionContext();
    ec.setContext(context);

    
    // --------------------------+
    // Add an implicit action.   |
    // --------------------------+
    ji.addImplicitAction(new PrintMeImplicitAction());
    
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
