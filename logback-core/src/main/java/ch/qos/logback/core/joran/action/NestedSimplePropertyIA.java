/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.joran.action;



import java.util.Stack;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.ExecutionContext;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.util.PropertySetter;



/**
 * This action is responsible for tying together a parent object with
 * one of its <em>simple</em> properties specified as an element but for 
 * which there is no explicit rule.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class NestedSimplePropertyIA extends ImplicitAction {
  
  // actionDataStack contains ActionData instances
  // We use a stack of ActionData objects in order to support nested
  // elements which are handled by the same NestedPropertyIA instance.
  // We push a ActionData instance in the isApplicable method (if the
  // action is applicable) and pop it in the end() method.
  // The XML well-formedness property will guarantee that a push will eventually
  // be followed by the corresponding pop.
  Stack<ImplicitActionData> actionDataStack = new Stack<ImplicitActionData>();

  public boolean isApplicable(
    Pattern pattern, Attributes attributes, ExecutionContext ec) {
    //LogLog.debug("in NestComponentIA.isApplicable <" + pattern + ">");
    String nestedElementTagName = pattern.peekLast();

    Object o = ec.peekObject();
    PropertySetter parentBean = new PropertySetter(o);

    int containmentType = parentBean.canContainComponent(nestedElementTagName);

    switch (containmentType) {
    case PropertySetter.NOT_FOUND:
    case PropertySetter.AS_COMPONENT:
    case PropertySetter.AS_COLLECTION:
      return false;

    case PropertySetter.AS_PROPERTY:
      ImplicitActionData ad = new ImplicitActionData(parentBean, containmentType);
      ad.propertyName = nestedElementTagName;
      actionDataStack.push(ad);
      // System.out.println("NestedSimplePropertyIA deemed applicable for " +pattern);
      return true;
    default:
      addError("PropertySetter.canContainComponent returned " + containmentType);
      return false;
    }
  }

  public void begin(
    ExecutionContext ec, String localName, Attributes attributes) {
    // NOP
  }

  public void body(ExecutionContext ec, String body) {

    String finalBody = ec.subst(body);
    // get the action data object pushed in isApplicable() method call
    ImplicitActionData actionData = (ImplicitActionData) actionDataStack.peek();
    actionData.parentBean.setProperty(actionData.propertyName, finalBody);
    
  }
  
  public void end(ExecutionContext ec, String tagName) {
    // pop the action data object pushed in isApplicable() method call
    actionDataStack.pop();
  }
}
