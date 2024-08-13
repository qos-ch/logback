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
import java.util.function.Supplier;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.GenericXMLConfigurator;
import ch.qos.logback.core.joran.JoranConfiguratorBase;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.util.VariableSubstitutionsHelper;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.ContextAwarePropertyContainer;
import ch.qos.logback.core.spi.PropertyContainer;

public class ModelInterpretationContext extends ContextAwareBase implements ContextAwarePropertyContainer {

    Stack<Object> objectStack;
    Stack<Model> modelStack;

    /**
     * A supplier of JoranConfigurator instances.
     *
     * May be null.
     *
     * @since 1.5.5
     */
    Supplier<? extends GenericXMLConfigurator> configuratorSupplier;


    Map<String, Object> objectMap;
    protected VariableSubstitutionsHelper variableSubstitutionsHelper;
    protected Map<String, String> importMap;

    final private BeanDescriptionCache beanDescriptionCache;
    final DefaultNestedComponentRegistry defaultNestedComponentRegistry = new DefaultNestedComponentRegistry();
    List<DependencyDefinition> dependencyDefinitionList = new ArrayList<>();
    final List<String> startedDependees = new ArrayList<>();

    Object configuratorHint;

    Model topModel;

    public ModelInterpretationContext(Context context) {
        this(context, null);
    }

    public ModelInterpretationContext(Context context, Object configuratorHint) {
        this.context = context;
        this.configuratorHint = configuratorHint;
        this.objectStack = new Stack<>();
        this.modelStack = new Stack<>();
        this.beanDescriptionCache = new BeanDescriptionCache(context);
        objectMap = new HashMap<>(5);
        variableSubstitutionsHelper = new VariableSubstitutionsHelper(context);
        importMap = new HashMap<>(5);
    }

    public ModelInterpretationContext(ModelInterpretationContext otherMic) {
        this(otherMic.context, otherMic.configuratorHint);
        importMap = new HashMap<>(otherMic.importMap);
        variableSubstitutionsHelper =  new VariableSubstitutionsHelper(context, otherMic.getCopyOfPropertyMap());
        defaultNestedComponentRegistry.duplicate(otherMic.getDefaultNestedComponentRegistry());
        createAppenderBags();
    } 
        
    public Map<String, Object> getObjectMap() {
        return objectMap;
    }

    public void createAppenderBags() {
        objectMap.put(JoranConstants.APPENDER_BAG, new HashMap<String, Appender<?>>());
        objectMap.put(JoranConstants.APPENDER_REF_BAG, new HashMap<String, AppenderAttachable<?>>());
    }

    public Model getTopModel() {
        return topModel;
    }

    public void setTopModel(Model topModel) {
        this.topModel = topModel;
    }

    // modelStack =================================

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

    public Object getConfiguratorHint() {
        return configuratorHint;
    }

    public void setConfiguratorHint(Object configuratorHint) {
        this.configuratorHint = configuratorHint;
    }

    public BeanDescriptionCache getBeanDescriptionCache() {
        return beanDescriptionCache;
    }

    public String subst(String ref)  {

        String substituted = variableSubstitutionsHelper.subst(ref);
        if(ref != null && !ref.equals(substituted)) {
            addInfo("value \""+substituted+"\" substituted for \""+ref+"\"");
        }
        return substituted;
    }


    public DefaultNestedComponentRegistry getDefaultNestedComponentRegistry() {
        return defaultNestedComponentRegistry;
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
     * Add a property to the properties of this execution context. If the property
     * exists already, it is overwritten.
     */
    @Override
    public void addSubstitutionProperty(String key, String value) {
        variableSubstitutionsHelper.addSubstitutionProperty(key, value);
    }

    /**
     * If a key is found in propertiesMap then return it. Otherwise, delegate to the
     * context.
     */
    public String getProperty(String key) {
      return  variableSubstitutionsHelper.getProperty(key);
    }

    @Override
    public Map<String, String> getCopyOfPropertyMap() {
        return variableSubstitutionsHelper.getCopyOfPropertyMap();
    }

    // imports ===================================================================

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

    public Map<String, String> getImportMapCopy() {
        return new HashMap<>(importMap);
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

    /**
     * Returns a supplier of {@link GenericXMLConfigurator} instance. The returned value may be null.
     *
     * @return a supplier of {@link GenericXMLConfigurator} instance, may be null
     */
    public Supplier<? extends GenericXMLConfigurator> getConfiguratorSupplier() {
        return this.configuratorSupplier;
    }

    /**
     *
     * @param configuratorSupplier
     */
    public void setConfiguratorSupplier(Supplier<? extends GenericXMLConfigurator> configuratorSupplier) {
        this.configuratorSupplier = configuratorSupplier;
    }
}
