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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import org.xml.sax.Locator;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.event.InPlayListener;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyContainer;
import ch.qos.logback.core.util.OptionHelper;

/**
 * 
 * An InterpretationContext contains the contextual state of a Joran parsing
 * session. {@link Action} objects depend on this context to exchange and store
 * information.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class InterpretationContext extends ContextAwareBase implements PropertyContainer {
    Stack<Object> objectStack;
    Map<String, Object> objectMap;
    Map<String, String> propertiesMap;

    Interpreter joranInterpreter;
    final List<InPlayListener> listenerList = new ArrayList<InPlayListener>();
    DefaultNestedComponentRegistry defaultNestedComponentRegistry = new DefaultNestedComponentRegistry();

    public InterpretationContext(Context context, Interpreter joranInterpreter) {
        this.context = context;
        this.joranInterpreter = joranInterpreter;
        objectStack = new Stack<Object>();
        objectMap = new HashMap<String, Object>(5);
        propertiesMap = new HashMap<String, String>(5);
    }

    public DefaultNestedComponentRegistry getDefaultNestedComponentRegistry() {
        return defaultNestedComponentRegistry;
    }

    public Map<String, String> getCopyOfPropertyMap() {
        return new HashMap<String, String>(propertiesMap);
    }

    void setPropertiesMap(Map<String, String> propertiesMap) {
        this.propertiesMap = propertiesMap;
    }

    String updateLocationInfo(String msg) {
        Locator locator = joranInterpreter.getLocator();

        if (locator != null) {
            return msg + locator.getLineNumber() + ":" + locator.getColumnNumber();
        } else {
            return msg;
        }
    }

    public Locator getLocator() {
        return joranInterpreter.getLocator();
    }

    public Interpreter getJoranInterpreter() {
        return joranInterpreter;
    }

    public Stack<Object> getObjectStack() {
        return objectStack;
    }

    public boolean isEmpty() {
        return objectStack.isEmpty();
    }

    public Object peekObject() {
        return objectStack.peek();
    }

    public void pushObject(Object o) {
        objectStack.push(o);
    }

    public Object popObject() {
        return objectStack.pop();
    }

    public Object getObject(int i) {
        return objectStack.get(i);
    }

    public Map<String, Object> getObjectMap() {
        return objectMap;
    }

    /**
     * Add a property to the properties of this execution context. If the property
     * exists already, it is overwritten.
     */
    public void addSubstitutionProperty(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        // values with leading or trailing spaces are bad. We remove them now.
        value = value.trim();
        propertiesMap.put(key, value);
    }

    public void addSubstitutionProperties(Properties props) {
        if (props == null) {
            return;
        }
        for (Object keyObject : props.keySet()) {
            String key = (String) keyObject;
            String val = props.getProperty(key);
            addSubstitutionProperty(key, val);
        }
    }

    /**
     * If a key is found in propertiesMap then return it. Otherwise, delegate to
     * the context.
     */
    public String getProperty(String key) {
        String v = propertiesMap.get(key);
        if (v != null) {
            return v;
        } else {
            return context.getProperty(key);
        }
    }

    public String subst(String value) {
        if (value == null) {
            return null;
        }
        return OptionHelper.substVars(value, this, context);
    }

    public boolean isListenerListEmpty() {
        return listenerList.isEmpty();
    }

    public void addInPlayListener(InPlayListener ipl) {
        if (listenerList.contains(ipl)) {
            addWarn("InPlayListener " + ipl + " has been already registered");
        } else {
            listenerList.add(ipl);
        }
    }

    public boolean removeInPlayListener(InPlayListener ipl) {
        return listenerList.remove(ipl);
    }

    void fireInPlay(SaxEvent event) {
        for (InPlayListener ipl : listenerList) {
            ipl.inPlay(event);
        }
    }
}
