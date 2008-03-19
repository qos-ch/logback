/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran.spi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ImplicitAction;
import ch.qos.logback.core.joran.event.BodyEvent;
import ch.qos.logback.core.joran.event.EndEvent;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.StartEvent;
import ch.qos.logback.core.spi.ContextAwareImpl;

/**
 * <id>Interpreter</id> is Joran's main driving class. It extends SAX
 * {@link org.xml.sax.helpers.DefaultHandler DefaultHandler} which invokes
 * various {@link Action actions} according to predefined patterns.
 * 
 * <p>
 * Patterns are kept in a {@link RuleStore} which is programmed to store and
 * then later produce the applicable actions for a given pattern.
 * 
 * <p>
 * The pattern corresponding to a top level &lt;a&gt; element is the string
 * <id>"a"</id>.
 * 
 * <p>
 * The pattern corresponding to an element &lt;b&gt; embedded within a top level
 * &lt;a&gt; element is the string <id>"a/b"</id>.
 * 
 * <p>
 * The pattern corresponding to an &lt;b&gt; and any level of nesting is
 * "&#42;/b. Thus, the &#42; character placed at the beginning of a pattern
 * serves as a wildcard for the level of nesting.
 * 
 * Conceptually, this is very similar to the API of commons-digester. Joran
 * offers several small advantages. First and foremost, it offers support for
 * implicit actions which result in a significant leap in flexibility. Second,
 * in our opinion better error reporting capability. Third, it is self-reliant.
 * It does not depend on other APIs, in particular commons-logging which is too
 * unreliable. Last but not least, Joran is quite tiny and is expected to remain
 * so.
 * 
 * @author Ceki G&uuml;lcu&uuml;
 * 
 */
public class Interpreter  {
  private static List EMPTY_LIST = new Vector(0);
  
  final private RuleStore ruleStore;
  final private InterpretationContext ec;
  final private ArrayList<ImplicitAction> implicitActions;
  final private CAI_WithLocatorSupport cai;
  Pattern pattern;
  Locator locator;
  EventPlayer player;

   /**
   * The <id>actionListStack</id> contains a list of actions that are executing
   * for the given XML element.
   * 
   * A list of actions is pushed by the {link #startElement} and popped by
   * {@link #endElement}.
   * 
   */
  Stack<List> actionListStack;

  /**
   * If the skip nested is set, then we skip all its nested elements until it is
   * set back to null at when the element's end is reached.
   */
  Pattern skip = null;

  public Interpreter(Context context, RuleStore rs) {
    this.cai = new CAI_WithLocatorSupport(this);
    this.cai.setContext(context);
    ruleStore = rs; 
    ec = new InterpretationContext(context, this);
    implicitActions = new ArrayList<ImplicitAction>(3);
    pattern = new Pattern();
    actionListStack = new Stack<List>();
    player = new EventPlayer(this);
  }

  public InterpretationContext getExecutionContext() {
    return ec;
  }

  public void startDocument() {
  }

  public void startElement(StartEvent se) {
    setDocumentLocator(se.getLocator());
    startElement(se.namespaceURI, se.localName, se.qName, se.attributes);
  }
  
  private void startElement(String namespaceURI, String localName, String qName,
      Attributes atts) {

    String tagName = getTagName(localName, qName);

    // System.out.println("startElement [" + tagName + "]");

    pattern.push(tagName);

    List applicableActionList = getApplicableActionList(pattern, atts);
 
    if (applicableActionList != null) {
      actionListStack.add(applicableActionList);
      callBeginAction(applicableActionList, tagName, atts);
    } else {
      actionListStack.add(EMPTY_LIST);

      String errMsg = "no applicable action for [" + tagName
          + "], current pattern is [" + pattern + "]";
      cai.addError(errMsg);
    }
  }

  public void characters(BodyEvent be) {

    setDocumentLocator(be.locator);
    
    String body = be.getText();
    List applicableActionList = (List) actionListStack.peek();

    if (body != null) {
      body = body.trim();
    }
    if (body.length() > 0) {
      // System.out.println("calling body method with ["+body+ "]");
      callBodyAction(applicableActionList, body);
    }
  }

  public void endElement(EndEvent endEvent) {
    setDocumentLocator(endEvent.locator);
    endElement(endEvent.namespaceURI, endEvent.localName, endEvent.qName);
  }

  private void endElement(String namespaceURI, String localName, String qName) {
    List applicableActionList = (List) actionListStack.pop();
    // System.out.println("endElement ["+getTagName(localName, qName)+"]");

    if (skip != null) {
      // System.err.println("In End, pattern is "+pattern+", skip pattern
      // "+skip);
      if (skip.equals(pattern)) {
        // ec.addInfo("Normal processing will continue with the next element.
        // Current pattern is ["+ pattern+"]", this);
        skip = null;
      } else {
        // getLogger().debug("Skipping invoking end() method for [{}].",
        // pattern);
      }
    } else if (applicableActionList != EMPTY_LIST) {
      callEndAction(applicableActionList, getTagName(localName, qName));
    }

    // given that we always push, we must also pop the pattern
    pattern.pop();
  }

  public Locator getLocator() {
    return locator;
  }

  public void setDocumentLocator(Locator l) {
    locator = l;
  }

  String getTagName(String localName, String qName) {
    String tagName = localName;

    if ((tagName == null) || (tagName.length() < 1)) {
      tagName = qName;
    }

    return tagName;
  }

  public void addImplicitAction(ImplicitAction ia) {
    implicitActions.add(ia);
  }

  /**
   * Check if any implicit actions are applicable. As soon as an applicable
   * action is found, it is returned. Thus, the returned list will have at most
   * one element.
   */
  List lookupImplicitAction(Pattern pattern, Attributes attributes,
      InterpretationContext ec) {
    int len = implicitActions.size();

    for (int i = 0; i < len; i++) {
      ImplicitAction ia = (ImplicitAction) implicitActions.get(i);

      if (ia.isApplicable(pattern, attributes, ec)) {
        List<Action> actionList = new ArrayList<Action>(1);
        actionList.add(ia);

        return actionList;
      }
    }

    return null;
  }

  /**
   * Return the list of applicable patterns for this
   */
  List getApplicableActionList(Pattern pattern, Attributes attributes) {
    List applicableActionList = ruleStore.matchActions(pattern);

    // logger.debug("set of applicable patterns: " + applicableActionList);
    if (applicableActionList == null) {
      applicableActionList = lookupImplicitAction(pattern, attributes, ec);
    }

    return applicableActionList;
  }

  void callBeginAction(List applicableActionList, String tagName,
      Attributes atts) {
    if (applicableActionList == null) {
      return;
    }

    if (skip != null) {
      // getLogger().debug("Skipping invoking begin() method for [{}].",
      // pattern);
      return;
    }

    Iterator i = applicableActionList.iterator();

    while (i.hasNext()) {
      Action action = (Action) i.next();

      // now let us invoke the action. We catch and report any eventual
      // exceptions
      try {
        action.begin(ec, tagName, atts);
      } catch (ActionException ae) {
        switch (ae.getSkipCode()) {
        case ActionException.SKIP_CHILDREN:
          skip = (Pattern) pattern.clone();
          break;
        case ActionException.SKIP_SIBLINGS:
          skip = (Pattern) pattern.clone();
          // pretend the exception came from one level up. This will cause
          // all children and following siblings elements to be skipped
          skip.pop();
          break;
        }
        // getLogger().info("Skip pattern set to [{}]", skip);
      } catch (Exception e) {
        skip = (Pattern) pattern.clone();
        // getLogger().info("Skip pattern set to [{}]", skip);
        cai.addError("Exception in Action for tag [" + tagName + "]", e);
      }
    }
  }

  private void callBodyAction(List applicableActionList, String body) {
    if (applicableActionList == null) {
      return;
    }
    Iterator i = applicableActionList.iterator();

    while (i.hasNext()) {
      Action action = (Action) i.next();
      try {
        action.body(ec, body);
      } catch (ActionException ae) {
        cai.addError("Exception in end() methd for action [" + action + "]", ae);
      }
    }
  }

  private void callEndAction(List applicableActionList, String tagName) {
    if (applicableActionList == null) {
      return;
    }

    // logger.debug("About to call end actions on node: [" + localName + "]");
    Iterator i = applicableActionList.iterator();

    while (i.hasNext()) {
      Action action = (Action) i.next();
      // now let us invoke the end method of the action. We catch and report
      // any eventual exceptions
      try {
        action.end(ec, tagName);
      } catch (ActionException ae) {
        switch (ae.getSkipCode()) {
        case ActionException.SKIP_CHILDREN:
          // after end() is called there can't be any children
          break;
        case ActionException.SKIP_SIBLINGS:
          skip = (Pattern) pattern.clone();
          skip.pop();
          break;
        }
        // getLogger().info("Skip pattern set to <{}>", skip);
      } catch (Exception e) {
        cai.addError("Exception in Action for tag [" + tagName + "]", e);
        skip = (Pattern) pattern.clone();
        skip.pop(); // induce the siblings to be skipped
        // getLogger().info("Skip pattern set to [{}].", skip);
      }
    }
  }

  public RuleStore getRuleStore() {
    return ruleStore;
  }
  
  public void play(List<SaxEvent> eventList) {
    player.play(eventList);
  }
  
  public void addEvents(List<SaxEvent> eventList) {
    if (player != null) {
      player.addEvents(eventList);
    }
  }
}

/**
 * When {@link Interpreter} class is used as the origin of an 
 * {@link ContextAwareImpl} instance, then XML locator information
 * is lost. This class preserves locator information (as a string).
 * 
 * @author ceki
 */
class CAI_WithLocatorSupport extends ContextAwareImpl {
  
  CAI_WithLocatorSupport(Interpreter interpreter) {
    super(interpreter);
  }
  
  @Override
  protected Object getOrigin() {
    Interpreter i = (Interpreter) super.getOrigin();
    Locator locator = i.locator;
    if(locator != null) {
      return Interpreter.class.getName()+"@"+locator.getLineNumber()+":"+locator.getColumnNumber();
    } else {
      return Interpreter.class.getName()+"@NA:NA";
    }
  }
}
