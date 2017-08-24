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
package ch.qos.logback.classic.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LoggerNameUtilTest {

    @Test
    public void smoke0() {
        List<String> witnessList = new ArrayList<String>();
        witnessList.add("a");
        witnessList.add("b");
        witnessList.add("c");
        List<String> partList = LoggerNameUtil.computeNameParts("a.b.c");
        assertEquals(witnessList, partList);
    }

    @Test
    public void smoke1() {
        List<String> witnessList = new ArrayList<String>();
        witnessList.add("com");
        witnessList.add("foo");
        witnessList.add("Bar");
        List<String> partList = LoggerNameUtil.computeNameParts("com.foo.Bar");
        assertEquals(witnessList, partList);
    }

    @Test
    public void emptyStringShouldReturnAListContainingOneEmptyString() {
        List<String> witnessList = new ArrayList<String>();
        witnessList.add("");
        List<String> partList = LoggerNameUtil.computeNameParts("");
        assertEquals(witnessList, partList);
    }

    @Test
    public void dotAtLastPositionShouldReturnAListWithAnEmptyStringAsLastElement() {
        List<String> witnessList = new ArrayList<String>();
        witnessList.add("com");
        witnessList.add("foo");
        witnessList.add("");

        List<String> partList = LoggerNameUtil.computeNameParts("com.foo.");
        assertEquals(witnessList, partList);
    }

    @Test
    public void supportNestedClasses() {
        List<String> witnessList = new ArrayList<String>();
        witnessList.add("com");
        witnessList.add("foo");
        witnessList.add("Bar");
        witnessList.add("Nested");

        List<String> partList = LoggerNameUtil.computeNameParts("com.foo.Bar$Nested");
        assertEquals(witnessList, partList);
    }

    @Test
    public void supportNestedClassesWithNestedDot() {
        // LOGBACK-384
        List<String> witnessList = new ArrayList<String>();
        witnessList.add("com");
        witnessList.add("foo");
        witnessList.add("Bar");
        witnessList.add("Nested");
        witnessList.add("dot");

        List<String> partList = LoggerNameUtil.computeNameParts("com.foo.Bar$Nested.dot");
        assertEquals(witnessList, partList);
    }

    @Test
    public void supportNestedClassesAtBeginning() {
        List<String> witnessList = new ArrayList<String>();
        witnessList.add("foo");
        witnessList.add("Nested");
        witnessList.add("bar");

        List<String> partList = LoggerNameUtil.computeNameParts("foo$Nested.bar");
        assertEquals(witnessList, partList);
    }

}
