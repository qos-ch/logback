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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class LoggerNameUtilTest {

    @Test
    public void smoke0() {
        final List<String> witnessList = new ArrayList<>();
        witnessList.add("a");
        witnessList.add("b");
        witnessList.add("c");
        final List<String> partList = LoggerNameUtil.computeNameParts("a.b.c");
        assertEquals(witnessList, partList);
    }

    @Test
    public void smoke1() {
        final List<String> witnessList = new ArrayList<>();
        witnessList.add("com");
        witnessList.add("foo");
        witnessList.add("Bar");
        final List<String> partList = LoggerNameUtil.computeNameParts("com.foo.Bar");
        assertEquals(witnessList, partList);
    }

    @Test
    public void emptyStringShouldReturnAListContainingOneEmptyString() {
        final List<String> witnessList = new ArrayList<>();
        witnessList.add("");
        final List<String> partList = LoggerNameUtil.computeNameParts("");
        assertEquals(witnessList, partList);
    }

    @Test
    public void dotAtLastPositionShouldReturnAListWithAnEmptyStringAsLastElement() {
        final List<String> witnessList = new ArrayList<>();
        witnessList.add("com");
        witnessList.add("foo");
        witnessList.add("");

        final List<String> partList = LoggerNameUtil.computeNameParts("com.foo.");
        assertEquals(witnessList, partList);
    }

    @Test
    public void supportNestedClasses() {
        final List<String> witnessList = new ArrayList<>();
        witnessList.add("com");
        witnessList.add("foo");
        witnessList.add("Bar");
        witnessList.add("Nested");

        final List<String> partList = LoggerNameUtil.computeNameParts("com.foo.Bar$Nested");
        assertEquals(witnessList, partList);
    }

    @Test
    public void supportNestedClassesWithNestedDot() {
        // LOGBACK-384
        final List<String> witnessList = new ArrayList<>();
        witnessList.add("com");
        witnessList.add("foo");
        witnessList.add("Bar");
        witnessList.add("Nested");
        witnessList.add("dot");

        final List<String> partList = LoggerNameUtil.computeNameParts("com.foo.Bar$Nested.dot");
        assertEquals(witnessList, partList);
    }

    @Test
    public void supportNestedClassesAtBeginning() {
        final List<String> witnessList = new ArrayList<>();
        witnessList.add("foo");
        witnessList.add("Nested");
        witnessList.add("bar");

        final List<String> partList = LoggerNameUtil.computeNameParts("foo$Nested.bar");
        assertEquals(witnessList, partList);
    }

}
