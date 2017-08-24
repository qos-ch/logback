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
// Contributors:  Georg Lundesgaard
package ch.qos.logback.core.joran.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import ch.qos.logback.core.joran.spi.DefaultClass;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.util.beans.BeanDescription;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.joran.util.beans.BeanUtil;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.AggregationType;
import ch.qos.logback.core.util.PropertySetterException;

/**
 * General purpose Object property setter. Clients repeatedly invokes
 * {@link #setProperty setProperty(name,value)} in order to invoke setters on
 * the Object specified in the constructor. This class relies on the JavaBeans
 * {@link Introspector} to analyze the given Object Class using reflection.
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
 *
 * will cause the invocations anObject.setName("Joe"), anObject.setAge(32), and
 * setMale(true) if such methods exist with those signatures. Otherwise an
 * {@link IntrospectionException} are thrown.
 *
 * @author Anders Kristensen
 * @author Ceki Gulcu
 */
public class PropertySetter extends ContextAwareBase {

    protected final Object obj;
    protected final Class<?> objClass;
    protected final BeanDescription beanDescription;

    /**
     * Create a new PropertySetter for the specified Object. This is done in
     * preparation for invoking {@link #setProperty} one or more times.
     *
     * @param obj
     *          the object for which to set properties
     */
    public PropertySetter(BeanDescriptionCache beanDescriptionCache, Object obj) {
        this.obj = obj;
        this.objClass = obj.getClass();
        this.beanDescription = beanDescriptionCache.getBeanDescription(objClass);
    }

    /**
     * Set a property on this PropertySetter's Object. If successful, this method
     * will invoke a setter method on the underlying Object. The setter is the one
     * for the specified property name and the value is determined partly from the
     * setter argument type and partly from the value specified in the call to
     * this method.
     *
     * <p>
     * If the setter expects a String no conversion is necessary. If it expects an
     * int, then an attempt is made to convert 'value' to an int using new
     * Integer(value). If the setter expects a boolean, the conversion is by new
     * Boolean(value).
     *
     * @param name
     *          name of the property
     * @param value
     *          String value of the property
     */
    public void setProperty(String name, String value) {
        if (value == null) {
            return;
        }
        Method setter = findSetterMethod(name);
        if (setter == null) {
            addWarn("No setter for property [" + name + "] in " + objClass.getName() + ".");
        } else {
            try {
                setProperty(setter, name, value);
            } catch (PropertySetterException ex) {
                addWarn("Failed to set property [" + name + "] to value \"" + value + "\". ", ex);
            }
        }
    }

    /**
     * Set the named property given a {@link PropertyDescriptor}.
     *
     * @param prop
     *          A PropertyDescriptor describing the characteristics of the
     *          property to set.
     * @param name
     *          The named of the property to set.
     * @param value
     *          The value of the property.
     */
    private void setProperty(Method setter, String name, String value) throws PropertySetterException {
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
        String cName = capitalizeFirstLetter(name);

        Method addMethod = findAdderMethod(cName);

        if (addMethod != null) {
            AggregationType type = computeRawAggregationType(addMethod);
            switch (type) {
            case NOT_FOUND:
                return AggregationType.NOT_FOUND;
            case AS_BASIC_PROPERTY:
                return AggregationType.AS_BASIC_PROPERTY_COLLECTION;

            case AS_COMPLEX_PROPERTY:
                return AggregationType.AS_COMPLEX_PROPERTY_COLLECTION;
            case AS_BASIC_PROPERTY_COLLECTION:
            case AS_COMPLEX_PROPERTY_COLLECTION:
                addError("Unexpected AggregationType " + type);
            }
        }

        Method setter = findSetterMethod(name);
        if (setter != null) {
            return computeRawAggregationType(setter);
        } else {
            // we have failed
            return AggregationType.NOT_FOUND;
        }
    }

    private Method findAdderMethod(String name) {
        String propertyName = BeanUtil.toLowerCamelCase(name);
        return beanDescription.getAdder(propertyName);
    }

    private Method findSetterMethod(String name) {
        String propertyName = BeanUtil.toLowerCamelCase(name);
        return beanDescription.getSetter(propertyName);
    }

    private Class<?> getParameterClassForMethod(Method method) {
        if (method == null) {
            return null;
        }
        Class<?>[] classArray = method.getParameterTypes();
        if (classArray.length != 1) {
            return null;
        } else {
            return classArray[0];
        }
    }

    private AggregationType computeRawAggregationType(Method method) {
        Class<?> parameterClass = getParameterClassForMethod(method);
        if (parameterClass == null) {
            return AggregationType.NOT_FOUND;
        }
        if (StringToObjectConverter.canBeBuiltFromSimpleString(parameterClass)) {
            return AggregationType.AS_BASIC_PROPERTY;
        } else {
            return AggregationType.AS_COMPLEX_PROPERTY;
        }
    }

    /**
     * Can the given clazz instantiable with certainty?
     *
     * @param clazz
     *          The class to test for instantiability
     * @return true if clazz can be instantiated, and false otherwise.
     */
    private boolean isUnequivocallyInstantiable(Class<?> clazz) {
        if (clazz.isInterface()) {
            return false;
        }
        // checking for constructors would be more elegant, but in
        // classes without any declared constructors, Class.getConstructor()
        // returns null.
        Object o;
        try {
            o = clazz.newInstance();
            if (o != null) {
                return true;
            } else {
                return false;
            }
        } catch (InstantiationException e) {
            return false;
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    public Class<?> getObjClass() {
        return objClass;
    }

    public void addComplexProperty(String name, Object complexProperty) {
        Method adderMethod = findAdderMethod(name);
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
            addError("Could not invoke method " + method.getName() + " in class " + obj.getClass().getName() + " with parameter of type " + ccc.getName(), e);
        }
    }

    public void addBasicProperty(String name, String strValue) {

        if (strValue == null) {
            return;
        }

        name = capitalizeFirstLetter(name);
        Method adderMethod = findAdderMethod(name);

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
            invokeMethodWithSingleParameterOnThisObject(adderMethod, strValue);
        }
    }

    public void setComplexProperty(String name, Object complexProperty) {
        Method setter = findSetterMethod(name);

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
            addError("Wrong number of parameters in setter method for property [" + name + "] in " + obj.getClass().getName());

            return false;
        }

        if (!params[0].isAssignableFrom(complexProperty.getClass())) {
            addError("A \"" + ccc.getName() + "\" object is not assignable to a \"" + params[0].getName() + "\" variable.");
            addError("The class \"" + params[0].getName() + "\" was loaded by ");
            addError("[" + params[0].getClassLoader() + "] whereas object of type ");
            addError("\"" + ccc.getName() + "\" was loaded by [" + ccc.getClassLoader() + "].");
            return false;
        }

        return true;
    }

    private String capitalizeFirstLetter(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public Object getObj() {
        return obj;
    }

    Method getRelevantMethod(String name, AggregationType aggregationType) {
        Method relevantMethod;
        if (aggregationType == AggregationType.AS_COMPLEX_PROPERTY_COLLECTION) {
            relevantMethod = findAdderMethod(name);
        } else if (aggregationType == AggregationType.AS_COMPLEX_PROPERTY) {
            relevantMethod = findSetterMethod(name);
        } else {
            throw new IllegalStateException(aggregationType + " not allowed here");
        }
        return relevantMethod;
    }

    <T extends Annotation> T getAnnotation(String name, Class<T> annonationClass, Method relevantMethod) {

        if (relevantMethod != null) {
            return relevantMethod.getAnnotation(annonationClass);
        } else {
            return null;
        }
    }

    Class<?> getDefaultClassNameByAnnonation(String name, Method relevantMethod) {
        DefaultClass defaultClassAnnon = getAnnotation(name, DefaultClass.class, relevantMethod);
        if (defaultClassAnnon != null) {
            return defaultClassAnnon.value();
        }
        return null;
    }

    Class<?> getByConcreteType(String name, Method relevantMethod) {

        Class<?> paramType = getParameterClassForMethod(relevantMethod);
        if (paramType == null) {
            return null;
        }

        boolean isUnequivocallyInstantiable = isUnequivocallyInstantiable(paramType);
        if (isUnequivocallyInstantiable) {
            return paramType;
        } else {
            return null;
        }

    }

    public Class<?> getClassNameViaImplicitRules(String name, AggregationType aggregationType, DefaultNestedComponentRegistry registry) {

        Class<?> registryResult = registry.findDefaultComponentType(obj.getClass(), name);
        if (registryResult != null) {
            return registryResult;
        }
        // find the relevant method for the given property name and aggregationType
        Method relevantMethod = getRelevantMethod(name, aggregationType);
        if (relevantMethod == null) {
            return null;
        }
        Class<?> byAnnotation = getDefaultClassNameByAnnonation(name, relevantMethod);
        if (byAnnotation != null) {
            return byAnnotation;
        }
        return getByConcreteType(name, relevantMethod);
    }

}
