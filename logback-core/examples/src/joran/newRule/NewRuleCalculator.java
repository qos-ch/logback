/**
 * Logback: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package joran.newRule;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import joran.calculator.ComputationAction2;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.NewRuleAction;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SimpleRuleStore;
import ch.qos.logback.core.util.StatusPrinter;


/**
 * This example illustrates the usage of NewRuleAction which allows the Joran
 * interpreter to learn new rules on the fly from the XML file being
 * interpreted.
 *
 * This example relies heavily on the code from the joran.calculator package.
 *
 * @author Ceki G&uuml;ulc&uuml;
 */
public class NewRuleCalculator {
  public static void main(String[] args) throws Exception {
    // As usual, we create a simple rule store.
    Context context = new ContextBase();
    RuleStore ruleStore = new SimpleRuleStore(context);
    
    // we start with the rule for the top-most (root) element
    ruleStore.addRule(new Pattern("*/computation"), new ComputationAction2());

    // Associate "/new-rule" pattern with NewRuleAction from the 
    // org.apache.joran.action package.
    // 
    // We will let the XML file to teach the Joran interpreter about new rules 
    ruleStore.addRule(
      new Pattern("/computation/new-rule"), new NewRuleAction());

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
