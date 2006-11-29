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
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.ContainmentType;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.PropertySetter;

/**
 * This action is responsible for tying together a parent object with a child
 * element for which there is no explicit rule.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class NestedComponentIA extends ImplicitAction {

  // actionDataStack contains ActionData instances
  // We use a stack of ActionData objects in order to support nested
  // elements which are handled by the same NestComponentIA instance.
  // We push a ActionData instance in the isApplicable method (if the
  // action is applicable) and pop it in the end() method.
  // The XML well-formedness property will guarantee that a push will eventually
  // be followed by the corresponding pop.
  Stack<ImplicitActionData> actionDataStack = new Stack<ImplicitActionData>();

  public boolean isApplicable(Pattern pattern, Attributes attributes,
      InterpretationContext ec) {
    // System.out.println("in NestComponentIA.isApplicable [" + pattern + "]");
    String nestedElementTagName = pattern.peekLast();

    // calling ec.peekObject with an empty stack will throw an exception
    if (ec.isEmpty()) {
      return false;
    }

    Object o = ec.peekObject();
    PropertySetter parentBean = new PropertySetter(o);
    parentBean.setContext(context);

    ContainmentType containmentType = parentBean
        .canContainComponent(nestedElementTagName);

    switch (containmentType) {
    case NOT_FOUND:
    case AS_SINGLE_PROPERTY:
    case AS_PROPERTY_COLLECTION:
      return false;

      // we only push action data if NestComponentIA is applicable
    case AS_COMPONENT_COLLECTION:
    case AS_SINGLE_COMPONENT:
      //addInfo("was deemed applicable for " + pattern);
      ImplicitActionData ad = new ImplicitActionData(parentBean,
          containmentType);
      actionDataStack.push(ad);

      return true;
    default:
      addError("PropertySetter.canContainComponent returned " + containmentType);
      return false;
    }
  }

  public void begin(InterpretationContext ec, String localName,
      Attributes attributes) {
    // LogLog.debug("in NestComponentIA begin method");
    // get the action data object pushed in isApplicable() method call
    ImplicitActionData actionData = (ImplicitActionData) actionDataStack.peek();

    String className = attributes.getValue(CLASS_ATTRIBUTE);

    // perform variable name substitution
    className = ec.subst(className);

    if (OptionHelper.isEmpty(className)) {
      actionData.inError = true;
      String errMsg = "No class name attribute in [" + localName + "]";
      addError(errMsg);

      return;
    }

    try {
      // getLogger().debug(
      // "About to instantiate component [{}] of type [{}]", localName,
      // className);

      // FIXME: Loading classes should be governed by config file rules.
      actionData.nestedComponent = Loader.loadClass(className).newInstance();

      // pass along the repository
      if (actionData.nestedComponent instanceof ContextAware) {
        ((ContextAware) actionData.nestedComponent).setContext(this.context);
      }
      // getLogger().debug(
      addInfo("Pushing component [" + localName
          + "] on top of the object stack.");
      ec.pushObject(actionData.nestedComponent);
    } catch (Exception oops) {
      actionData.inError = true;
      String msg = "Could not create component [" + localName + "] of type ["
          + className + "]";
      addError(msg, oops);
    }
  }

  public void end(InterpretationContext ec, String tagName) {

    // pop the action data object pushed in isApplicable() method call
    // we assume that each this begin
    ImplicitActionData actionData = (ImplicitActionData) actionDataStack.pop();

    if (actionData.inError) {
      return;
    }

    PropertySetter nestedBean = new PropertySetter(actionData.nestedComponent);
    nestedBean.setContext(context);

    if (nestedBean.canContainComponent("parent") == ContainmentType.AS_SINGLE_COMPONENT) {
      nestedBean.setComponent("parent", actionData.parentBean.getObj());
    }
    if (actionData.nestedComponent instanceof LifeCycle) {
      ((LifeCycle) actionData.nestedComponent).start();
    }

    Object o = ec.peekObject();

    if (o != actionData.nestedComponent) {
      addError("The object on the top the of the stack is not the component pushed earlier.");
    } else {
      // getLogger().debug("Removing component from the object stack");
      ec.popObject();

      // Now let us attach the component
      switch (actionData.containmentType) {
      case AS_SINGLE_COMPONENT:
        // addInfo("Setting ["+tagName+"}] to parent of type
        // ["+actionData.parentBean.getObjClass()+"]");

        actionData.parentBean.setComponent(tagName, actionData.nestedComponent);

        break;
      case AS_COMPONENT_COLLECTION:
        // getLogger().debug(
        // "Adding [{}] to parent of type [{}]", tagName,
        // actionData.parentBean.getObjClass());
        actionData.parentBean.addComponent(tagName, actionData.nestedComponent);

        break;
      }
    }
  }

}
