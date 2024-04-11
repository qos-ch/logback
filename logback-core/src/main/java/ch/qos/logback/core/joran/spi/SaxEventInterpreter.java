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
package ch.qos.logback.core.joran.spi;

import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.event.BodyEvent;
import ch.qos.logback.core.joran.event.EndEvent;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.StartEvent;
import ch.qos.logback.core.spi.ContextAwareImpl;

/**
 * {@code SaxEventInterpreter} is Joran's driving class for handling "low-level"
 * SAX events. It extends SAX {@link org.xml.sax.helpers.DefaultHandler
 * DefaultHandler} which invokes various {@link Action actions} according to
 * predefined patterns.
 * 
 * <p>
 * Patterns are kept in a {@link RuleStore} which is programmed to store and
 * then later produce the applicable actions for a given pattern.
 * 
 * <p>
 * The pattern corresponding to a top level &lt;a&gt; element is the string "a".
 * 
 * <p>
 * The pattern corresponding to an element &lt;b&gt; embedded within a top level
 * &lt;a&gt; element is the string {@code "a/b"}.
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
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class SaxEventInterpreter {
    private static Action NOP_ACTION_SINGLETON = new NOPAction();

    final private RuleStore ruleStore;
    final private SaxEventInterpretationContext interpretationContext;
    private Supplier<Action> implicitActionSupplier;
    final private CAI_WithLocatorSupport cai;
    private ElementPath elementPath;
    Locator locator;
    EventPlayer eventPlayer;
    Context context;
    
    /**
     * The <id>actionStack</id> contain the action that is executing
     * for the given XML element.
     * 
     * An action is pushed by the {link #startElement} and popped by
     * {@link #endElement}.
     * 
     */
    Stack<Action> actionStack;

    /**
     * If the skip nested is set, then we skip all its nested elements until it is
     * set back to null at when the element's end is reached.
     */
    ElementPath skip = null;

    public SaxEventInterpreter(Context context, RuleStore rs, ElementPath initialElementPath, List<SaxEvent> saxEvents) {
        this.context = context;
        this.cai = new CAI_WithLocatorSupport(context, this);
        ruleStore = rs;
        interpretationContext = new SaxEventInterpretationContext(context, this);
        this.elementPath = initialElementPath;
        actionStack = new Stack<>();
        eventPlayer = new EventPlayer(this, saxEvents);
    }

    public EventPlayer getEventPlayer() {
        return eventPlayer;
    }

    public ElementPath getCopyOfElementPath() {
        return elementPath.duplicate();
    }

    public SaxEventInterpretationContext getSaxEventInterpretationContext() {
        return interpretationContext;
    }

    public void startDocument() {
    }

    public void startElement(StartEvent se) {
        setDocumentLocator(se.getLocator());
        startElement(se.namespaceURI, se.localName, se.qName, se.attributes);
    }

    private void startElement(String namespaceURI, String localName, String qName, Attributes atts) {

        String tagName = getTagName(localName, qName);
   
        
        elementPath.push(tagName);
   
        if (skip != null) {
            // every startElement pushes an action list
            pushEmptyActionOntoActionStack();
            return;
        }

        Action applicableAction = getApplicableAction(elementPath, atts);
        if (applicableAction != null) {
            actionStack.add(applicableAction);
            callBeginAction(applicableAction, tagName, atts);
        } else {
            // every startElement pushes an action list
            pushEmptyActionOntoActionStack();
            String errMsg = "no applicable action for [" + tagName + "], current ElementPath  is [" + elementPath + "]";
            cai.addError(errMsg);
        }
    }

    /**
     * This method is used to
     */
    private void pushEmptyActionOntoActionStack() {
       actionStack.push(NOP_ACTION_SINGLETON);       
    }

    public void characters(BodyEvent be) {

        setDocumentLocator(be.locator);

        String body = be.getText();
        Action applicableAction = actionStack.peek();

        if (body != null) {
            body = body.trim();
            if (body.length() > 0) {
                callBodyAction(applicableAction, body);
            }
        }
    }

    public void endElement(EndEvent endEvent) {
        setDocumentLocator(endEvent.locator);
        endElement(endEvent.namespaceURI, endEvent.localName, endEvent.qName);
    }

    private void endElement(String namespaceURI, String localName, String qName) {
        // given that an action is always pushed for every startElement, we
        // need to always pop for every endElement
        Action applicableAction =  actionStack.pop();

        if (skip != null) {
            if (skip.equals(elementPath)) {
                skip = null;
            }
        } else if (applicableAction != NOP_ACTION_SINGLETON) {
            callEndAction(applicableAction, getTagName(localName, qName));
        }

        // given that we always push, we must also pop the pattern
        elementPath.pop();
    }

    public Locator getLocator() {
        return locator;
    }

    // having the locator set as parsing progresses is quite ugly
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

    public void setImplicitActionSupplier(Supplier<Action> actionSupplier) {
        this.implicitActionSupplier = actionSupplier;
    }

    /**
     * Return the list of applicable patterns for this
     */
    Action getApplicableAction(ElementPath elementPath, Attributes attributes) {
        Supplier<Action> applicableActionSupplier = ruleStore.matchActions(elementPath);

        if (applicableActionSupplier != null) {
            Action applicableAction = applicableActionSupplier.get();
            applicableAction.setContext(context);
            return applicableAction;
        } else {
            Action implicitAction = implicitActionSupplier.get();
            implicitAction.setContext(context);
            return implicitAction;
        }
    }

    void callBeginAction(Action applicableAction, String tagName, Attributes atts) {
        if (applicableAction == null) {
            return;
        }

        // now let us invoke the action. We catch and report any eventual
        // exceptions
        try {
             applicableAction.begin(interpretationContext, tagName, atts);
        } catch (ActionException e) {
            skip = elementPath.duplicate();
            cai.addError("ActionException in Action for tag [" + tagName + "]", e);
        } catch (RuntimeException e) {
            skip = elementPath.duplicate();
            cai.addError("RuntimeException in Action for tag [" + tagName + "]", e);
        }
        
    }

    private void callBodyAction(Action applicableAction, String body) {
        if (applicableAction == null) {
            return;
        }

        try {
            applicableAction.body(interpretationContext, body);
        } catch (ActionException ae) {
            cai.addError("Exception in body() method for action [" + applicableAction + "]", ae);
        }
    }

    private void callEndAction(Action applicableAction, String tagName) {
        if (applicableAction == null) {
            return;
        }

        try {
            applicableAction.end(interpretationContext, tagName);
        } catch (ActionException ae) {
            // at this point endAction, there is no point in skipping children as
            // they have been already processed
            cai.addError("ActionException in Action for tag [" + tagName + "]", ae);
        } catch (RuntimeException e) {
            // no point in setting skip
            cai.addError("RuntimeException in Action for tag [" + tagName + "]", e);
        }
    }

    public RuleStore getRuleStore() {
        return ruleStore;
    }
}

/**
 * When {@link SaxEventInterpreter} class is used as the origin of an
 * {@link ContextAwareImpl} instance, then XML locator information is lost. This
 * class preserves locator information (as a string).
 * 
 * @author ceki
 */
class CAI_WithLocatorSupport extends ContextAwareImpl {

    CAI_WithLocatorSupport(Context context, SaxEventInterpreter interpreter) {
        super(context, interpreter);
    }

    @Override
    protected Object getOrigin() {
        SaxEventInterpreter i = (SaxEventInterpreter) super.getOrigin();
        Locator locator = i.locator;
        if (locator != null) {
            return SaxEventInterpreter.class.getName() + "@" + locator.getLineNumber() + ":"
                    + locator.getColumnNumber();
        } else {
            return SaxEventInterpreter.class.getName() + "@NA:NA";
        }
    }
}
