/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 * <p>
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 * <p>
 * or (per the licensee's choosing)
 * <p>
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
// Contributors:  Georg Lundesgaard
package ch.qos.logback.core.joran.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.util.beans.BeanDescription;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.AggregationType;
import ch.qos.logback.core.util.PropertySetterException;
import ch.qos.logback.core.util.StringUtil;

import java.lang.reflect.Method;

/**
 * General purpose Object property setter. Clients repeatedly invokes
 * {@link #setProperty setProperty(name,value)} in order to invoke setters on
 * the Object specified in the constructor. This class relies on reflection to
 * analyze the given Object Class.
 *
 * <p>
 * Usage:
 *
 * <pre>
 * PropertySetter ps = new PropertySetter(anObject);
 * ps.set(&quot;name&quot;, &quot;Joe&quot;);
 * ps.set(&quot;age&quot;, &quot;32&quot;);
 * ps.set(&quot;isMale&quot;, &quot;true&quot;);
 * </pre>
 * <p>
 * will cause the invocations anObject.setName("Joe"), anObject.setAge(32), and
 * setMale(true) if such methods exist with those signatures. Otherwise an
 * {@link PropertySetterException} is thrown.
 *
 * @author Anders Kristensen
 * @author Ceki Gulcu
 */
public class PropertySetter extends ContextAwareBase {

    protected final Object obj;
    protected final Class<?> objClass;
    protected final BeanDescription beanDescription;
    protected final AggregationAssessor aggregationAssessor;

    /**
     * Create a new PropertySetter for the specified Object. This is done in
     * preparation for invoking {@link #setProperty} one or more times.
     *
     * @param obj the object for which to set properties
     */
    public PropertySetter(BeanDescriptionCache beanDescriptionCache, Object obj) {
        this.obj = obj;
        this.objClass = obj.getClass();
        this.beanDescription = beanDescriptionCache.getBeanDescription(objClass);
        this.aggregationAssessor = new AggregationAssessor(beanDescriptionCache, this.objClass);
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        aggregationAssessor.setContext(context);
    }


    /**
     * Set a property on this PropertySetter's Object. If successful, this method
     * will invoke a setter method on the underlying Object. The setter is the one
     * for the specified property name and the value is determined partly from the
     * setter argument type and partly from the value specified in the call to this
     * method.
     *
     * <p>
     * If the setter expects a String no conversion is necessary. If it expects an
     * int, then an attempt is made to convert 'value' to an int using new
     * Integer(value). If the setter expects a boolean, the conversion is by new
     * Boolean(value).
     *
     * @param name  name of the property
     * @param value String value of the property
     */
    public void setProperty(String name, String value) {
        if (value == null) {
            return;
        }
        Method setter = aggregationAssessor.findSetterMethod(name);
        if (setter == null) {
            addWarn("No setter for property [" + name + "] in " + objClass.getName() + ".");
        } else {
            try {
                setProperty(setter, value);
            } catch (PropertySetterException ex) {
                addWarn("Failed to set property [" + name + "] to value \"" + value + "\". ", ex);
            }
        }
    }

    /**
     * Set the named property using a {@link Method setter}.
     *
     * @param setter A Method describing the characteristics of the
     *               property to set.
     * @param value  The value of the property.
     */
    private void setProperty(Method setter, String value) throws PropertySetterException {
        Class<?>[] paramTypes = setter.getParameterTypes();

        Object arg;

        try {
            arg = StringToObjectConverter.convertArg(this, value, paramTypes[0]);
        } catch (Throwable t) {
            throw new PropertySetterException("Conversion to type [" + paramTypes[0] + "] failed. ", t);
        }

        if (arg == null) {
            throw new PropertySetterException("Conversion to type [" + paramTypes[0] + "] failed.");
        }
        try {
            setter.invoke(obj, arg);
        } catch (Exception ex) {
            throw new PropertySetterException(ex);
        }
    }

    public AggregationType computeAggregationType(String name) {
        return this.aggregationAssessor.computeAggregationType(name);
    }


    public Class<?> getObjClass() {
        return objClass;
    }

    public void addComplexProperty(String name, Object complexProperty) {
        Method adderMethod = aggregationAssessor.findAdderMethod(name);
        // first let us use the addXXX method
        if (adderMethod != null) {
            Class<?>[] paramTypes = adderMethod.getParameterTypes();
            if (!isSanityCheckSuccessful(name, adderMethod, paramTypes, complexProperty)) {
                return;
            }
            invokeMethodWithSingleParameterOnThisObject(adderMethod, complexProperty);
        } else {
            addError("Could not find method [" + "add" + name + "] in class [" + objClass.getName() + "].");
        }
    }

    void invokeMethodWithSingleParameterOnThisObject(Method method, Object parameter) {
        Class<?> ccc = parameter.getClass();
        try {
            method.invoke(this.obj, parameter);
        } catch (Exception e) {
            addError("Could not invoke method " + method.getName() + " in class " + obj.getClass().getName()
                    + " with parameter of type " + ccc.getName(), e);
        }
    }

    public void addBasicProperty(String name, String strValue) {

        if (strValue == null) {
            return;
        }

        name = StringUtil.capitalizeFirstLetter(name);
        Method adderMethod = aggregationAssessor.findAdderMethod(name);

        if (adderMethod == null) {
            addError("No adder for property [" + name + "].");
            return;
        }

        Class<?>[] paramTypes = adderMethod.getParameterTypes();
        isSanityCheckSuccessful(name, adderMethod, paramTypes, strValue);

        Object arg;
        try {
            arg = StringToObjectConverter.convertArg(this, strValue, paramTypes[0]);
        } catch (Throwable t) {
            addError("Conversion to type [" + paramTypes[0] + "] failed. ", t);
            return;
        }
        if (arg != null) {
            invokeMethodWithSingleParameterOnThisObject(adderMethod, arg);
        }
    }

    public void setComplexProperty(String name, Object complexProperty) {
        Method setter = aggregationAssessor.findSetterMethod(name);

        if (setter == null) {
            addWarn("Not setter method for property [" + name + "] in " + obj.getClass().getName());

            return;
        }

        Class<?>[] paramTypes = setter.getParameterTypes();

        if (!isSanityCheckSuccessful(name, setter, paramTypes, complexProperty)) {
            return;
        }
        try {
            invokeMethodWithSingleParameterOnThisObject(setter, complexProperty);

        } catch (Exception e) {
            addError("Could not set component " + obj + " for parent component " + obj, e);
        }
    }

    private boolean isSanityCheckSuccessful(String name, Method method, Class<?>[] params, Object complexProperty) {
        Class<?> ccc = complexProperty.getClass();
        if (params.length != 1) {
            addError("Wrong number of parameters in setter method for property [" + name + "] in "
                    + obj.getClass().getName());

            return false;
        }

        if (!params[0].isAssignableFrom(complexProperty.getClass())) {
            addError("A \"" + ccc.getName() + "\" object is not assignable to a \"" + params[0].getName()
                    + "\" variable.");
            addError("The class \"" + params[0].getName() + "\" was loaded by ");
            addError("[" + params[0].getClassLoader() + "] whereas object of type ");
            addError("\"" + ccc.getName() + "\" was loaded by [" + ccc.getClassLoader() + "].");
            return false;
        }

        return true;
    }

    public Object getObj() {
        return obj;
    }


    public Class<?> getClassNameViaImplicitRules(String name, AggregationType aggregationType,
                                                 DefaultNestedComponentRegistry registry) {
        return aggregationAssessor.getClassNameViaImplicitRules(name, aggregationType, registry);
    }

    public Class<?> getTypeForComplexProperty(String nestedElementTagName, AggregationType aggregationType) {

        Method aMethod = null;
        switch (aggregationType) {
            case AS_COMPLEX_PROPERTY:
                aMethod = aggregationAssessor.findSetterMethod(nestedElementTagName);
                break;
            case AS_COMPLEX_PROPERTY_COLLECTION:
                aMethod = aggregationAssessor.findAdderMethod(nestedElementTagName);
        }


        checkParameterCount(aMethod, nestedElementTagName);

        Class<?>[] paramTypes = aMethod.getParameterTypes();
        return paramTypes[0];

    }

    private void checkParameterCount(Method aMethod, String nestedElementTagName) {
        if(aMethod == null) {
            String msg = "Could not find method for property [" + nestedElementTagName + "].";
            addError(msg);
            throw new IllegalStateException(msg);
        }
        int parameterCount = aMethod.getParameterCount();
        if (parameterCount != 1) {
            String msg = "Expected ["+aMethod.getName()+"] for property [" + nestedElementTagName + "] to have exactly one parameter.";
            addError(msg);
            throw new IllegalStateException(msg);
        }
    }
}
