/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.core.joran.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.util.AggregationType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AggregationAssessorTest {
    Context context = new ContextBase();
    AggregationAssessor aggregationAssessor = new AggregationAssessor(new BeanDescriptionCache(context), House.class);
    DefaultNestedComponentRegistry defaultComponentRegistry = new DefaultNestedComponentRegistry();

    @Test
    public void testgetClassNameViaImplicitRules() {
        Class<?> compClass = aggregationAssessor.getClassNameViaImplicitRules("door", AggregationType.AS_COMPLEX_PROPERTY,
                defaultComponentRegistry);
        assertEquals(Door.class, compClass);
    }

    @Test
    public void testgetComplexPropertyColleClassNameViaImplicitRules() {
        Class<?> compClass = aggregationAssessor.getClassNameViaImplicitRules("window",
                AggregationType.AS_COMPLEX_PROPERTY_COLLECTION, defaultComponentRegistry);
        assertEquals(Window.class, compClass);
    }

    @Test
    public void testDefaultClassAnnotationForLists() {
        Method relevantMethod = aggregationAssessor.getRelevantMethod("LargeSwimmingPool",
                AggregationType.AS_COMPLEX_PROPERTY_COLLECTION);
        assertNotNull(relevantMethod);
        Class<?> spClass = aggregationAssessor.getDefaultClassNameByAnnonation("LargeSwimmingPool", relevantMethod);
        assertEquals(LargeSwimmingPoolImpl.class, spClass);

        Class<?> classViaImplicitRules = aggregationAssessor.getClassNameViaImplicitRules("LargeSwimmingPool",
                AggregationType.AS_COMPLEX_PROPERTY_COLLECTION, defaultComponentRegistry);
        assertEquals(LargeSwimmingPoolImpl.class, classViaImplicitRules);
    }

    @Test
    public void testDefaultClassAnnonation() {
        Method relevantMethod = aggregationAssessor.getRelevantMethod("SwimmingPool", AggregationType.AS_COMPLEX_PROPERTY);
        assertNotNull(relevantMethod);
        Class<?> spClass = aggregationAssessor.getDefaultClassNameByAnnonation("SwimmingPool", relevantMethod);
        assertEquals(SwimmingPoolImpl.class, spClass);

        Class<?> classViaImplicitRules = aggregationAssessor.getClassNameViaImplicitRules("SwimmingPool",
                AggregationType.AS_COMPLEX_PROPERTY, defaultComponentRegistry);
        assertEquals(SwimmingPoolImpl.class, classViaImplicitRules);
    }

}
