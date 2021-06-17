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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import org.xml.sax.Locator;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ImplicitActionDataBase;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.model.Model;
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
	Stack<Model> modelStack;
	Stack<ImplicitActionDataBase> implicitActionDataStack;

	Map<String, Object> objectMap;
	Map<String, String> propertiesMap;

	SaxEventInterpreter saxEventInterpreter;
	DefaultNestedComponentRegistry defaultNestedComponentRegistry = new DefaultNestedComponentRegistry();
	private BeanDescriptionCache beanDescriptionCache;

	public InterpretationContext(Context context, SaxEventInterpreter saxEventInterpreter) {
		this.context = context;
		this.saxEventInterpreter = saxEventInterpreter;
		this.objectStack = new Stack<>();
		this.modelStack = new Stack<>();
		this.implicitActionDataStack = new Stack<>();

		objectMap = new HashMap<>(5);
		propertiesMap = new HashMap<>(5);
	}

	public BeanDescriptionCache getBeanDescriptionCache() {
		if (beanDescriptionCache == null) {
			beanDescriptionCache = new BeanDescriptionCache(getContext());
		}
		return beanDescriptionCache;
	}

	public DefaultNestedComponentRegistry getDefaultNestedComponentRegistry() {
		return defaultNestedComponentRegistry;
	}

	@Override
    public Map<String, String> getCopyOfPropertyMap() {
		return new HashMap<String, String>(propertiesMap);
	}

	void setPropertiesMap(Map<String, String> propertiesMap) {
		this.propertiesMap = propertiesMap;
	}

	String updateLocationInfo(String msg) {
		Locator locator = saxEventInterpreter.getLocator();

		if (locator != null) {
			return msg + locator.getLineNumber() + ":" + locator.getColumnNumber();
		} else {
			return msg;
		}
	}

	public String getLineNumber() {
		Locator locator = saxEventInterpreter.getLocator();

		if (locator != null) {
			return Integer.toString(locator.getLineNumber());
		} else {
			return "NA";
		}
	}

	public Locator getLocator() {
		return saxEventInterpreter.getLocator();
	}

	public SaxEventInterpreter getSaxEventInterpreter() {
		return saxEventInterpreter;
	}

	public Stack<Object> getObjectStack() {
		return objectStack;
	}

	/**
	 * @deprecated Use {@link isObjectStackEmpty isObjectStackEmpty()} method
	 *             instead
	 * @return
	 */
	public boolean isEmpty() {
		return isObjectStackEmpty();
	}

	/**
	 *
	 * @return whether the objectStack is empty or not
	 */
	public boolean isObjectStackEmpty() {
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

	/**
	 * actionDataStack contains ActionData instances We use a stack of ActionData
	 * objects in order to support nested elements which are handled by the same
	 * NestedComplexPropertyIA instance. We push a ActionData instance in the
	 * isApplicable method (if the action is applicable) and pop it in the end()
	 * method. The XML well-formedness property will guarantee that a push will
	 * eventually be followed by a corresponding pop.
	 */
	public Stack<ImplicitActionDataBase> getImplcitActionDataStack() {
		return implicitActionDataStack;
	}

	public Model peekModel() {
		return modelStack.peek();
	}

	public void pushModel(Model m) {
		modelStack.push(m);
	}

	public boolean isModelStackEmpty() {
		return modelStack.isEmpty();
	}

	public Model popModel() {
		return modelStack.pop();
	}

	public Stack<Model> getCopyOfModelStack() {
		Stack<Model> copy = new Stack<>();
		copy.addAll(modelStack);
		return copy;
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
	 * If a key is found in propertiesMap then return it. Otherwise, delegate to the
	 * context.
	 */
	@Override
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

}
