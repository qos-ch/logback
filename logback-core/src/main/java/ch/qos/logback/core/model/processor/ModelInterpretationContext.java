/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.model.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.ImplicitActionDataBase;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyContainer;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.util.OptionHelper;

public class ModelInterpretationContext extends ContextAwareBase implements PropertyContainer {

    Stack<Object> objectStack;
    Stack<Model> modelStack;

    Stack<ImplicitActionDataBase> implicitActionDataStack;

    Map<String, Object> objectMap;
    Map<String, String> propertiesMap;
    Map<String, String> importMap;


    final private BeanDescriptionCache beanDescriptionCache;
    final DefaultNestedComponentRegistry defaultNestedComponentRegistry = new DefaultNestedComponentRegistry();
    List<DependencyDefinition> dependencyDefinitionList = new ArrayList<>();
    final List<String> startedDependees = new ArrayList<>();

    public ModelInterpretationContext(Context context) {
        this.context = context;
        this.objectStack = new Stack<>();
        this.modelStack = new Stack<>();
        this.implicitActionDataStack = new Stack<>();
        this.beanDescriptionCache = new BeanDescriptionCache(context);
        objectMap = new HashMap<>(5);
        propertiesMap = new HashMap<>(5);
        importMap = new HashMap<>(5);
    }

    public Map<String, Object> getObjectMap() {
        return objectMap;
    }

    // moodelStack =================================

    public void pushModel(Model m) {
        modelStack.push(m);
    }

    public Model peekModel() {
        return modelStack.peek();
    }

    public boolean isModelStackEmpty() {
        return modelStack.isEmpty();
    }

    public Model popModel() {
        return modelStack.pop();
    }

    // =================== object stack

    public Stack<Object> getObjectStack() {
        return objectStack;
    }

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

    public Object getObject(int i) {
        return objectStack.get(i);
    }

    // ===================== END object stack

    public BeanDescriptionCache getBeanDescriptionCache() {
        return beanDescriptionCache;
    }

    public String subst(String ref) {
        if (ref == null) {
            return null;
        }

        try {
            return OptionHelper.substVars(ref, this, context);
        } catch (ScanException | IllegalArgumentException e) {
            addError("Problem while parsing [" + ref + "]", e);
            return ref;
        }

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

    public DefaultNestedComponentRegistry getDefaultNestedComponentRegistry() {
        return defaultNestedComponentRegistry;
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

    // ================================== dependencies

    public void addDependencyDefinition(DependencyDefinition dd) {
        dependencyDefinitionList.add(dd);
    }

    public List<DependencyDefinition> getDependencyDefinitions() {
        return Collections.unmodifiableList(dependencyDefinitionList);
    }

    public List<String> getDependeeNamesForModel(Model model) {
        List<String> dependencyList = new ArrayList<>();
        for (DependencyDefinition dd : dependencyDefinitionList) {
            if (dd.getDepender() == model) {
               dependencyList.add(dd.getDependee());
            }
        }
        return dependencyList;
    }

    public boolean hasDependers(String dependeeName) {

        if (dependeeName == null || dependeeName.trim().length() == 0) {
            new IllegalArgumentException("Empty dependeeName name not allowed here");
        }

        for (DependencyDefinition dd : dependencyDefinitionList) {
            if (dd.dependee.equals(dependeeName))
                return true;
        }

        return false;
    }


    public void markStartOfNamedDependee(String name) {
        startedDependees.add(name);
    }

    public boolean isNamedDependeeStarted(String name) {
        return startedDependees.contains(name);
    }

    // ========================================== object map

    /**
     * If a key is found in propertiesMap then return it. Otherwise, delegate to the
     * context.
     */
    public String getProperty(String key) {
        String v = propertiesMap.get(key);
        if (v != null) {
            return v;
        } else {
            return context.getProperty(key);
        }
    }

    @Override
    public Map<String, String> getCopyOfPropertyMap() {
        return new HashMap<String, String>(propertiesMap);
    }

    // imports

    /**
     * Add an import to the importMao
     * 
     * @param stem the class to import
     * @param fqcn the fully qualified name of the class
     * 
     * @since 1.3
     */
    public void addImport(String stem, String fqcn) {
        importMap.put(stem, fqcn);
    }

    /**
     * Given a stem, get the fully qualified name of the class corresponding to the
     * stem. For unknown stems, returns the stem as is. If stem is null, null is
     * returned.
     * 
     * @param stem may be null
     * @return fully qualified name of the class corresponding to the stem. For
     *         unknown stems, returns the stem as is. If stem is null, null is
     *         returned.
     * @since 1.3
     */
    public String getImport(String stem) {
        if (stem == null)
            return null;

        String result = importMap.get(stem);
        if (result == null)
            return stem;
        else
            return result;
    }

}
