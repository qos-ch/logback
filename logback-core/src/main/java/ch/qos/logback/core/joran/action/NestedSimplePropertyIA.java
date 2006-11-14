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

import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.util.ContainmentType;
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
    Pattern pattern, Attributes attributes, InterpretationContext ec) {
    //System.out.println("in NestedSimplePropertyIA.isApplicable <" + pattern + ">");
    String nestedElementTagName = pattern.peekLast();

    // no point in attempting if there is no parent object
    if(ec.isEmpty()) {
      return false;
    }
    
    Object o = ec.peekObject();
    PropertySetter parentBean = new PropertySetter(o);
    parentBean.setContext(context);
    
    ContainmentType containmentType = parentBean.canContainComponent(nestedElementTagName);

    switch (containmentType) {
    case NOT_FOUND:
    case AS_SINGLE_COMPONENT:
    case AS_COMPONENT_COLLECTION:
      return false;

    case AS_SINGLE_PROPERTY:
    case AS_PROPERTY_COLLECTION:
      ImplicitActionData ad = new ImplicitActionData(parentBean, containmentType);
      ad.propertyName = nestedElementTagName;
      actionDataStack.push(ad);
      //addInfo("NestedSimplePropertyIA deemed applicable <" + pattern + ">");
      return true;
    default:
      addError("PropertySetter.canContainComponent returned " + containmentType);
      return false;
    }
  }

  public void begin(
    InterpretationContext ec, String localName, Attributes attributes) {
    // NOP
  }

  public void body(InterpretationContext ec, String body) {
   
    String finalBody = ec.subst(body);
    //System.out.println("body "+body+", finalBody="+finalBody);
    // get the action data object pushed in isApplicable() method call
    ImplicitActionData actionData = (ImplicitActionData) actionDataStack.peek();
    switch (actionData.containmentType) {
    case AS_SINGLE_PROPERTY:
      actionData.parentBean.setProperty(actionData.propertyName, finalBody);
      break;
    case AS_PROPERTY_COLLECTION:
      actionData.parentBean.addProperty(actionData.propertyName, finalBody);
    }
  }
  
  public void end(InterpretationContext ec, String tagName) {
    // pop the action data object pushed in isApplicable() method call
    actionDataStack.pop();
  }
}
