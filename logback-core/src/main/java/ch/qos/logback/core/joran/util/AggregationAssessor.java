/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.core.joran.util;

import ch.qos.logback.core.joran.spi.DefaultClass;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.util.beans.BeanDescription;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.joran.util.beans.BeanUtil;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.AggregationType;
import ch.qos.logback.core.util.StringUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * Various utility methods for computing the {@link AggregationType} of a given property or
 * the class name of a property given implicit rules.
 *
 * <p>This class was extracted from {@link PropertySetter}. </p>
 *
 * @since 1.5.1
 */
public class AggregationAssessor extends ContextAwareBase  {

    protected final Class<?> objClass;
    protected final BeanDescription beanDescription;

    public AggregationAssessor(BeanDescriptionCache beanDescriptionCache, Class objClass) {
        this.objClass = objClass;
        this.beanDescription = beanDescriptionCache.getBeanDescription(objClass);
    }

    /**
     *  Given a property name, this method computes/assesses {@link AggregationType}
     *  for the property for the class passed to the constructor.
     *
     * @param name
     * @return the computed {@link AggregationType}
     */
    public AggregationType computeAggregationType(String name) {
        String cName = StringUtil.capitalizeFirstLetter(name);

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


//    String capitalizeFirstLetter(String name) {
//        return StringUtil.capitalizeFirstLetter(name);
//    }

    public Method findAdderMethod(String name) {
        String propertyName = BeanUtil.toLowerCamelCase(name);
        return beanDescription.getAdder(propertyName);
    }

    public Method findSetterMethod(String name) {
        String propertyName = BeanUtil.toLowerCamelCase(name);
        return beanDescription.getSetter(propertyName);
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

    public Class<?> getClassNameViaImplicitRules(String name, AggregationType aggregationType,
            DefaultNestedComponentRegistry registry) {

        Class<?> registryResult = registry.findDefaultComponentType(objClass, name);
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

    /**
     * Can the given clazz instantiable with certainty?
     *
     * @param clazz The class to test for instantiability
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
            o = clazz.getDeclaredConstructor().newInstance();
            if (o != null) {
                return true;
            } else {
                return false;
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException e) {
            return false;
        }
    }
}
