/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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

package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.ComponentFactory;
import ch.qos.logback.core.util.DynamicClassLoadingException;
import ch.qos.logback.core.util.IncompatibleClassException;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

/**
 * Handles the case where the configuration specifies a component factory. We just need to capture
 * this in our context for later use.
 * 
 * @author Rich Mayfield
 * 
 */
public class ComponentFactoryAction extends Action {

    public final static String CLASS_ATTRIBUTE = "class";

    /**
     * Captures the name of a component factory to be used when instantiating objects for this
     * context.
     * 
     * @see ch.qos.logback.core.joran.action.Action#begin(ch.qos.logback.core.joran.spi.InterpretationContext,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void begin(InterpretationContext ic, String name, Attributes attributes)
            throws ActionException {
        String className = attributes.getValue(CLASS_ATTRIBUTE);
        if (OptionHelper.isEmpty(className)) {
          addError("Attribute named [" + CLASS_ATTRIBUTE + "] cannot be empty");
          return;
        }
        
        try {
          ClassLoader classLoader = Loader.getClassLoaderOfObject(context);
          ComponentFactory factory = (ComponentFactory) OptionHelper.instantiateByClassName(className,
                ComponentFactory.class, classLoader);

          context.setComponentFactory(factory);
        } catch (Exception e) {
          addError("Could not create a ComponentFactory of type [" + className + "].");
        }
    }

    /**
     * We have nothing to do here.
     * 
     * @see ch.qos.logback.core.joran.action.Action#end(ch.qos.logback.core.joran.spi.InterpretationContext,
     *      java.lang.String)
     */
    @Override
    public void end(InterpretationContext ic, String name) throws ActionException {

    }

}
