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
package ch.qos.logback.core.joran.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.testUtil.StatusChecker;
import ch.qos.logback.core.util.AggregationType;
import ch.qos.logback.core.util.StatusPrinter;

public class PropertySetterTest {

    DefaultNestedComponentRegistry defaultComponentRegistry = new DefaultNestedComponentRegistry();

    Context context = new ContextBase();
    StatusChecker checker = new StatusChecker(context);
    House house = new House();

    PropertySetter setter = new PropertySetter(new BeanDescriptionCache(context), house);

    @Before
    public void setUp() {
        setter.setContext(context);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCanAggregateComponent() {
        assertEquals(AggregationType.AS_COMPLEX_PROPERTY, setter.computeAggregationType("door"));

        assertEquals(AggregationType.AS_BASIC_PROPERTY, setter.computeAggregationType("count"));
        assertEquals(AggregationType.AS_BASIC_PROPERTY, setter.computeAggregationType("Count"));

        assertEquals(AggregationType.AS_BASIC_PROPERTY, setter.computeAggregationType("name"));
        assertEquals(AggregationType.AS_BASIC_PROPERTY, setter.computeAggregationType("Name"));

        assertEquals(AggregationType.AS_BASIC_PROPERTY, setter.computeAggregationType("Duration"));
        assertEquals(AggregationType.AS_BASIC_PROPERTY, setter.computeAggregationType("fs"));

        assertEquals(AggregationType.AS_BASIC_PROPERTY, setter.computeAggregationType("open"));
        assertEquals(AggregationType.AS_BASIC_PROPERTY, setter.computeAggregationType("Open"));

        assertEquals(AggregationType.AS_COMPLEX_PROPERTY_COLLECTION, setter.computeAggregationType("Window"));
        assertEquals(AggregationType.AS_BASIC_PROPERTY_COLLECTION, setter.computeAggregationType("adjective"));

        assertEquals(AggregationType.AS_BASIC_PROPERTY, setter.computeAggregationType("filterReply"));
        assertEquals(AggregationType.AS_BASIC_PROPERTY, setter.computeAggregationType("houseColor"));
    }

    @Test
    public void testSetProperty() {
        {
            final House house = new House();
            final PropertySetter setter = new PropertySetter(new BeanDescriptionCache(context), house);
            setter.setProperty("count", "10");
            setter.setProperty("temperature", "33.1");

            setter.setProperty("name", "jack");
            setter.setProperty("open", "true");

            assertEquals(10, house.getCount());
            assertEquals(33.1d, house.getTemperature(), 0.01);
            assertEquals("jack", house.getName());
            assertTrue(house.isOpen());
        }

        {
            final House house = new House();
            final PropertySetter setter = new PropertySetter(new BeanDescriptionCache(context), house);
            setter.setProperty("Count", "10");
            setter.setProperty("Name", "jack");
            setter.setProperty("Open", "true");

            assertEquals(10, house.getCount());
            assertEquals("jack", house.getName());
            assertTrue(house.isOpen());
        }
    }

    @Test
    public void testSetCamelProperty() {
        setter.setProperty("camelCase", "trot");
        assertEquals("trot", house.getCamelCase());

        setter.setProperty("camelCase", "gh");
        assertEquals("gh", house.getCamelCase());
    }

    @Test
    public void testSetComplexProperty() {
        final Door door = new Door();
        setter.setComplexProperty("door", door);
        assertEquals(door, house.getDoor());
    }

    @Test
    public void testgetClassNameViaImplicitRules() {
        final Class<?> compClass = setter.getClassNameViaImplicitRules("door", AggregationType.AS_COMPLEX_PROPERTY, defaultComponentRegistry);
        assertEquals(Door.class, compClass);
    }

    @Test
    public void testgetComplexPropertyColleClassNameViaImplicitRules() {
        final Class<?> compClass = setter.getClassNameViaImplicitRules("window", AggregationType.AS_COMPLEX_PROPERTY_COLLECTION, defaultComponentRegistry);
        assertEquals(Window.class, compClass);
    }

    @Test
    public void testPropertyCollection() {
        setter.addBasicProperty("adjective", "nice");
        setter.addBasicProperty("adjective", "big");

        assertEquals(2, house.adjectiveList.size());
        assertEquals("nice", house.adjectiveList.get(0));
        assertEquals("big", house.adjectiveList.get(1));
    }

    @Test
    public void testComplexCollection() {
        final Window w1 = new Window();
        w1.handle = 10;
        final Window w2 = new Window();
        w2.handle = 20;

        setter.addComplexProperty("window", w1);
        setter.addComplexProperty("window", w2);
        assertEquals(2, house.windowList.size());
        assertEquals(10, house.windowList.get(0).handle);
        assertEquals(20, house.windowList.get(1).handle);
    }

    @Test
    public void testSetComplexWithCamelCaseName() {
        final SwimmingPool pool = new SwimmingPoolImpl();
        setter.setComplexProperty("swimmingPool", pool);
        assertEquals(pool, house.getSwimmingPool());
    }

    @Test
    public void testDuration() {
        setter.setProperty("duration", "1.4 seconds");
        assertEquals(1400, house.getDuration().getMilliseconds());
    }

    @Test
    public void testFileSize() {
        setter.setProperty("fs", "2 kb");
        assertEquals(2 * 1024, house.getFs().getSize());
    }

    @Test
    public void testFilterReply() {
        // test case reproducing bug #52
        setter.setProperty("filterReply", "ACCEPT");
        assertEquals(FilterReply.ACCEPT, house.getFilterReply());
    }

    @Test
    public void testEnum() {
        setter.setProperty("houseColor", "BLUE");
        assertEquals(HouseColor.BLUE, house.getHouseColor());
    }

    @Test
    public void testDefaultClassAnnonation() {
        final Method relevantMethod = setter.getRelevantMethod("SwimmingPool", AggregationType.AS_COMPLEX_PROPERTY);
        assertNotNull(relevantMethod);
        final Class<?> spClass = setter.getDefaultClassNameByAnnonation("SwimmingPool", relevantMethod);
        assertEquals(SwimmingPoolImpl.class, spClass);

        final Class<?> classViaImplicitRules = setter.getClassNameViaImplicitRules("SwimmingPool", AggregationType.AS_COMPLEX_PROPERTY, defaultComponentRegistry);
        assertEquals(SwimmingPoolImpl.class, classViaImplicitRules);
    }

    @Test
    public void testDefaultClassAnnotationForLists() {
        final Method relevantMethod = setter.getRelevantMethod("LargeSwimmingPool", AggregationType.AS_COMPLEX_PROPERTY_COLLECTION);
        assertNotNull(relevantMethod);
        final Class<?> spClass = setter.getDefaultClassNameByAnnonation("LargeSwimmingPool", relevantMethod);
        assertEquals(LargeSwimmingPoolImpl.class, spClass);

        final Class<?> classViaImplicitRules = setter.getClassNameViaImplicitRules("LargeSwimmingPool", AggregationType.AS_COMPLEX_PROPERTY_COLLECTION,
                        defaultComponentRegistry);
        assertEquals(LargeSwimmingPoolImpl.class, classViaImplicitRules);
    }

    @Test
    public void charset() {
        setter.setProperty("charset", "UTF-8");
        assertEquals(Charset.forName("UTF-8"), house.getCharset());

        house.setCharset(null);
        setter.setProperty("charset", "UTF");
        assertNull(house.getCharset());

        final StatusChecker checker = new StatusChecker(context);
        checker.containsException(UnsupportedCharsetException.class);
    }

    // see also http://jira.qos.ch/browse/LOGBACK-1164
    @Test
    public void bridgeMethodsShouldBeIgnored() {
        final Orange orange = new Orange();

        final PropertySetter orangeSetter = new PropertySetter(new BeanDescriptionCache(context), orange);
        assertEquals(AggregationType.AS_BASIC_PROPERTY, orangeSetter.computeAggregationType(Citrus.PRECARP_PROPERTY_NAME));
        assertEquals(AggregationType.AS_BASIC_PROPERTY, orangeSetter.computeAggregationType(Citrus.PREFIX_PROPERTY_NAME));

        StatusPrinter.print(context);
        checker.assertIsWarningOrErrorFree();
    }
}
