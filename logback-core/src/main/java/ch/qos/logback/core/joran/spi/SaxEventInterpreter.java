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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.event.BodyEvent;
import ch.qos.logback.core.joran.event.EndEvent;
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
    private static List<Action> EMPTY_LIST = new ArrayList<>(0);

    final private RuleStore ruleStore;
    final private InterpretationContext interpretationContext;
    final private ArrayList<Action> implicitActions;
    final private CAI_WithLocatorSupport cai;
    private ElementPath elementPath;
    Locator locator;
    EventPlayer eventPlayer;

    /**
     * The <id>actionListStack</id> contains a list of actions that are executing
     * for the given XML element.
     *
     * A list of actions is pushed by the {link #startElement} and popped by
     * {@link #endElement}.
     *
     */
    Stack<List<Action>> actionListStack;

    /**
     * If the skip nested is set, then we skip all its nested elements until it is
     * set back to null at when the element's end is reached.
     */
    ElementPath skip = null;

    public SaxEventInterpreter(final Context context, final RuleStore rs, final ElementPath initialElementPath) {
        cai = new CAI_WithLocatorSupport(context, this);
        ruleStore = rs;
        interpretationContext = new InterpretationContext(context, this);
        implicitActions = new ArrayList<>(3);
        elementPath = initialElementPath;
        actionListStack = new Stack<>();
        eventPlayer = new EventPlayer(this);
    }

    public SaxEventInterpreter duplicate(final ElementPath initial) {
        final SaxEventInterpreter clone = new SaxEventInterpreter(cai.getContext(), ruleStore, initial);
        clone.addImplicitActions(implicitActions);
        clone.elementPath = initial;
        return clone;
    }

    public EventPlayer getEventPlayer() {
        return eventPlayer;
    }

    public ElementPath getCopyOfElementPath() {
        return elementPath.duplicate();
    }

    public void setInterpretationContextPropertiesMap(final Map<String, String> propertiesMap) {
        interpretationContext.setPropertiesMap(propertiesMap);
    }

    public InterpretationContext getInterpretationContext() {
        return interpretationContext;
    }

    public void startDocument() {
    }

    public void startElement(final StartEvent se) {
        setDocumentLocator(se.getLocator());
        startElement(se.namespaceURI, se.localName, se.qName, se.attributes);
    }

    private void startElement(final String namespaceURI, final String localName, final String qName, final Attributes atts) {

        final String tagName = getTagName(localName, qName);
        elementPath.push(tagName);

        if (skip != null) {
            // every startElement pushes an action list
            pushEmptyActionList();
            return;
        }

        final List<Action> applicableActionList = getApplicableActionList(elementPath, atts);
        if (applicableActionList != null) {
            actionListStack.add(applicableActionList);
            callBeginAction(applicableActionList, tagName, atts);
        } else {
            // every startElement pushes an action list
            pushEmptyActionList();
            final String errMsg = "no applicable action for [" + tagName + "], current ElementPath  is [" + elementPath + "]";
            cai.addError(errMsg);
        }
    }

    /**
     * This method is used to
     */
    private void pushEmptyActionList() {
        actionListStack.add(EMPTY_LIST);
    }

    public void characters(final BodyEvent be) {

        setDocumentLocator(be.locator);

        String body = be.getText();
        final List<Action> applicableActionList = actionListStack.peek();

        if (body != null) {
            body = body.trim();
            if (body.length() > 0) {
                // System.out.println("calling body method with ["+body+ "]");
                callBodyAction(applicableActionList, body);
            }
        }
    }

    public void endElement(final EndEvent endEvent) {
        setDocumentLocator(endEvent.locator);
        endElement(endEvent.namespaceURI, endEvent.localName, endEvent.qName);
    }

    private void endElement(final String namespaceURI, final String localName, final String qName) {
        // given that an action list is always pushed for every startElement, we
        // need
        // to always pop for every endElement
        final List<Action> applicableActionList = actionListStack.pop();

        if (skip != null) {
            if (skip.equals(elementPath)) {
                skip = null;
            }
        } else if (applicableActionList != EMPTY_LIST) {
            callEndAction(applicableActionList, getTagName(localName, qName));
        }

        // given that we always push, we must also pop the pattern
        elementPath.pop();
    }

    public Locator getLocator() {
        return locator;
    }

    // having the locator set as parsing progresses is quite ugly
    public void setDocumentLocator(final Locator l) {
        locator = l;
    }

    String getTagName(final String localName, final String qName) {
        String tagName = localName;

        if (tagName == null || tagName.length() < 1) {
            tagName = qName;
        }

        return tagName;
    }

    public void addImplicitActions(final List<Action> actionList) {
        implicitActions.addAll(actionList);
    }

    public void addImplicitAction(final Action ia) {
        implicitActions.add(ia);
    }

    /**
     * Return the list of applicable patterns for this
     */
    List<Action> getApplicableActionList(final ElementPath elementPath, final Attributes attributes) {
        List<Action> applicableActionList = ruleStore.matchActions(elementPath);

        if (applicableActionList == null) {
            applicableActionList = implicitActions;
        }

        return applicableActionList;
    }

    void callBeginAction(final List<Action> applicableActionList, final String tagName, final Attributes atts) {
        if (applicableActionList == null) {
            return;
        }

        final Iterator<Action> i = applicableActionList.iterator();
        while (i.hasNext()) {
            final Action action = i.next();
            // now let us invoke the action. We catch and report any eventual
            // exceptions
            try {
                action.begin(interpretationContext, tagName, atts);
            } catch (final ActionException e) {
                skip = elementPath.duplicate();
                cai.addError("ActionException in Action for tag [" + tagName + "]", e);
            } catch (final RuntimeException e) {
                skip = elementPath.duplicate();
                cai.addError("RuntimeException in Action for tag [" + tagName + "]", e);
            }
        }
    }

    private void callBodyAction(final List<Action> applicableActionList, final String body) {
        if (applicableActionList == null) {
            return;
        }
        final Iterator<Action> i = applicableActionList.iterator();

        while (i.hasNext()) {
            final Action action = i.next();
            try {
                action.body(interpretationContext, body);
            } catch (final ActionException ae) {
                cai.addError("Exception in end() methd for action [" + action + "]", ae);
            }
        }
    }

    private void callEndAction(final List<Action> applicableActionList, final String tagName) {
        if (applicableActionList == null) {
            return;
        }

        // logger.debug("About to call end actions on node: [" + localName + "]");
        final Iterator<Action> i = applicableActionList.iterator();

        while (i.hasNext()) {
            final Action action = i.next();
            // now let us invoke the end method of the action. We catch and report
            // any eventual exceptions
            try {
                action.end(interpretationContext, tagName);
            } catch (final ActionException ae) {
                // at this point endAction, there is no point in skipping children as
                // they have been already processed
                cai.addError("ActionException in Action for tag [" + tagName + "]", ae);
            } catch (final RuntimeException e) {
                // no point in setting skip
                cai.addError("RuntimeException in Action for tag [" + tagName + "]", e);
            }
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

    CAI_WithLocatorSupport(final Context context, final SaxEventInterpreter interpreter) {
        super(context, interpreter);
    }

    @Override
    protected Object getOrigin() {
        final SaxEventInterpreter i = (SaxEventInterpreter) super.getOrigin();
        final Locator locator = i.locator;
        if (locator != null) {
            return SaxEventInterpreter.class.getName() + "@" + locator.getLineNumber() + ":" + locator.getColumnNumber();
        }
        return SaxEventInterpreter.class.getName() + "@NA:NA";
    }
}
