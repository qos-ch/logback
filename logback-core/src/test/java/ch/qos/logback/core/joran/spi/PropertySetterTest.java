/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.util.AggregationType;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.FileSize;

public class PropertySetterTest {

  DefaultNestedComponentRegistry defaultComponentRegistry = new DefaultNestedComponentRegistry();

  @Test
  public void testCanAggregateComponent() {
    House house = new House();
    PropertySetter setter = new PropertySetter(house);
    assertEquals(AggregationType.AS_COMPLEX_PROPERTY, setter
        .computeAggregationType("door"));

    assertEquals(AggregationType.AS_BASIC_PROPERTY, setter
        .computeAggregationType("count"));
    assertEquals(AggregationType.AS_BASIC_PROPERTY, setter
        .computeAggregationType("Count"));

    assertEquals(AggregationType.AS_BASIC_PROPERTY, setter
        .computeAggregationType("name"));
    assertEquals(AggregationType.AS_BASIC_PROPERTY, setter
        .computeAggregationType("Name"));

    assertEquals(AggregationType.AS_BASIC_PROPERTY, setter
        .computeAggregationType("Duration"));
    assertEquals(AggregationType.AS_BASIC_PROPERTY, setter
        .computeAggregationType("fs"));

    assertEquals(AggregationType.AS_BASIC_PROPERTY, setter
        .computeAggregationType("open"));
    assertEquals(AggregationType.AS_BASIC_PROPERTY, setter
        .computeAggregationType("Open"));

    assertEquals(AggregationType.AS_COMPLEX_PROPERTY_COLLECTION, setter
        .computeAggregationType("Window"));
    assertEquals(AggregationType.AS_BASIC_PROPERTY_COLLECTION, setter
        .computeAggregationType("adjective"));

    assertEquals(AggregationType.AS_BASIC_PROPERTY, setter
        .computeAggregationType("filterReply"));
    assertEquals(AggregationType.AS_BASIC_PROPERTY, setter
        .computeAggregationType("houseColor"));

    System.out.println();
  }

  @Test
  public void testSetProperty() {
    {
      House house = new House();
      PropertySetter setter = new PropertySetter(house);
      setter.setProperty("count", "10");
      setter.setProperty("name", "jack");
      setter.setProperty("open", "true");

      assertEquals(10, house.getCount());
      assertEquals("jack", house.getName());
      assertTrue(house.isOpen());
    }

    {
      House house = new House();
      PropertySetter setter = new PropertySetter(house);
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
    House house = new House();
    PropertySetter setter = new PropertySetter(house);

    setter.setProperty("camelCase", "trot");
    assertEquals("trot", house.getCamelCase());

    setter.setProperty("camelCase", "gh");
    assertEquals("gh", house.getCamelCase());
  }

  @Test
  public void testSetComplexProperty() {
    House house = new House();
    Door door = new Door();
    PropertySetter setter = new PropertySetter(house);
    setter.setComplexProperty("door", door);
    assertEquals(door, house.getDoor());
  }

  @Test
  public void testgetClassNameViaImplicitRules() {
    House house = new House();
    PropertySetter setter = new PropertySetter(house);
    Class compClass = setter.getClassNameViaImplicitRules("door",
        AggregationType.AS_COMPLEX_PROPERTY, defaultComponentRegistry);
    assertEquals(Door.class, compClass);
  }

  @Test
  public void testgetComplexPropertyColleClassNameViaImplicitRules() {
    House house = new House();
    PropertySetter setter = new PropertySetter(house);
    Class compClass = setter.getClassNameViaImplicitRules("window",
        AggregationType.AS_COMPLEX_PROPERTY_COLLECTION,
        defaultComponentRegistry);
    assertEquals(Window.class, compClass);
  }

  @Test
  public void testPropertyCollection() {
    House house = new House();
    Context context = new ContextBase();
    PropertySetter setter = new PropertySetter(house);
    setter.setContext(context);
    setter.addBasicProperty("adjective", "nice");
    setter.addBasicProperty("adjective", "big");

    assertEquals(2, house.adjectiveList.size());
    assertEquals("nice", house.adjectiveList.get(0));
    assertEquals("big", house.adjectiveList.get(1));
  }

  @Test
  public void testComplexCollection() {
    House house = new House();
    PropertySetter setter = new PropertySetter(house);
    Window w1 = new Window();
    w1.handle = 10;
    Window w2 = new Window();
    w2.handle = 20;

    setter.addComplexProperty("window", w1);
    setter.addComplexProperty("window", w2);
    assertEquals(2, house.windowList.size());
    assertEquals(10, house.windowList.get(0).handle);
    assertEquals(20, house.windowList.get(1).handle);
  }

  @Test
  public void testSetComplexWithCamelCaseName() {
    House house = new House();
    SwimmingPool pool = new SwimmingPoolImpl();
    PropertySetter setter = new PropertySetter(house);
    setter.setComplexProperty("swimmingPool", pool);
    assertEquals(pool, house.getSwimmingPool());
  }

  @Test
  public void testDuration() {
    House house = new House();
    PropertySetter setter = new PropertySetter(house);
    setter.setProperty("duration", "1.4 seconds");
    assertEquals(1400, house.getDuration().getMilliseconds());
  }

  @Test
  public void testFileSize() {
    House house = new House();
    PropertySetter setter = new PropertySetter(house);
    setter.setProperty("fs", "2 kb");
    assertEquals(2 * 1024, house.getFs().getSize());
  }

  @Test
  public void testFilterReply() {
    // test case reproducing bug #52
    House house = new House();
    PropertySetter setter = new PropertySetter(house);
    setter.setProperty("filterReply", "ACCEPT");
    assertEquals(FilterReply.ACCEPT, house.getFilterReply());
  }

  @Test
  public void testEnum() {
    House house = new House();
    PropertySetter setter = new PropertySetter(house);
    setter.setProperty("houseColor", "BLUE");
    assertEquals(HouseColor.BLUE, house.getHouseColor());
  }

  @Test
  public void testDefaultClassAnnonation() {
    House house = new House();
    PropertySetter setter = new PropertySetter(house);
    Method relevantMethod = setter.getRelevantMethod("SwimmingPool",
        AggregationType.AS_COMPLEX_PROPERTY);
    assertNotNull(relevantMethod);
    Class spClass = setter.getDefaultClassNameByAnnonation("SwimmingPool",
        relevantMethod);
    assertEquals(SwimmingPoolImpl.class, spClass);

    Class classViaImplicitRules = setter.getClassNameViaImplicitRules(
        "SwimmingPool", AggregationType.AS_COMPLEX_PROPERTY,
        defaultComponentRegistry);
    assertEquals(SwimmingPoolImpl.class, classViaImplicitRules);
  }
  
  @Test
  public void testDefaultClassAnnotationForLists() {
    House house = new House();
    PropertySetter setter = new PropertySetter(house);
    Method relevantMethod = setter.getRelevantMethod("LargeSwimmingPool",
        AggregationType.AS_COMPLEX_PROPERTY_COLLECTION);
    assertNotNull(relevantMethod);
    Class spClass = setter.getDefaultClassNameByAnnonation("LargeSwimmingPool",
        relevantMethod);
    assertEquals(LargeSwimmingPoolImpl.class, spClass);

    Class classViaImplicitRules = setter.getClassNameViaImplicitRules(
        "LargeSwimmingPool", AggregationType.AS_COMPLEX_PROPERTY_COLLECTION,
        defaultComponentRegistry);
    assertEquals(LargeSwimmingPoolImpl.class, classViaImplicitRules);
    
  }
}

class House {
  Door mainDoor;
  int count;
  boolean open;
  String name;
  String camelCase;
  SwimmingPool pool;
  Duration duration;
  FileSize fs;
  HouseColor houseColor;
  FilterReply reply;

  List<String> adjectiveList = new ArrayList<String>();
  List<Window> windowList = new ArrayList<Window>();
  List<SwimmingPool> largePoolList = new ArrayList<SwimmingPool>();

  public String getCamelCase() {
    return camelCase;
  }

  public void setCamelCase(String camelCase) {
    this.camelCase = camelCase;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int c) {
    this.count = c;
  }

  public Door getDoor() {
    return mainDoor;
  }

  public void setDoor(Door door) {
    this.mainDoor = door;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isOpen() {
    return open;
  }

  public void setOpen(boolean open) {
    this.open = open;
  }

  @DefaultClass(LargeSwimmingPoolImpl.class)
  public void addLargeSwimmingPool(SwimmingPool pool) {
    this.pool = pool;
  }

  @DefaultClass(SwimmingPoolImpl.class)
  public void setSwimmingPool(SwimmingPool pool) {
    this.pool = pool;
  }

  public SwimmingPool getSwimmingPool() {
    return pool;
  }

  public void addWindow(Window w) {
    windowList.add(w);
  }

  public void addAdjective(String s) {
    adjectiveList.add(s);
  }

  public Duration getDuration() {
    return duration;
  }

  public void setDuration(Duration duration) {
    this.duration = duration;
  }

  public FileSize getFs() {
    return fs;
  }

  public void setFs(FileSize fs) {
    this.fs = fs;
  }

  public void setHouseColor(HouseColor color) {
    this.houseColor = color;
  }

  public HouseColor getHouseColor() {
    return houseColor;
  }

  public void setFilterReply(FilterReply reply) {
    this.reply = reply;
  }

  public FilterReply getFilterReply() {
    return reply;
  }

}

class Door {
  int handle;
}

class Window {
  int handle;
}

interface SwimmingPool {
}

class SwimmingPoolImpl implements SwimmingPool {
  int length;
  int width;
  int depth;
}

class LargeSwimmingPoolImpl implements SwimmingPool {
  int length;
  int width;
  int depth;
}

enum HouseColor {
  WHITE, BLUE
}
