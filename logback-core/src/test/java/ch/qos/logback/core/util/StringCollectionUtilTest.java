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
package ch.qos.logback.core.util;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link StringCollectionUtil}.
 *
 * @author Carl Harris
 */
public class StringCollectionUtilTest {

    @Test
    public void testRetainMatchingWithNoPatterns() throws Exception {
        Collection<String> values = stringToList("A");
        StringCollectionUtil.retainMatching(values);
        assertTrue(values.contains("A"));
    }

    @Test
    public void testRetainMatchingWithMatchingPattern() throws Exception {
        Collection<String> values = stringToList("A");
        StringCollectionUtil.retainMatching(values, "A");
        assertTrue(values.contains("A"));
    }

    @Test
    public void testRetainMatchingWithNoMatchingPattern() throws Exception {
        Collection<String> values = stringToList("A");
        StringCollectionUtil.retainMatching(values, "B");
        assertTrue(values.isEmpty());
    }

    @Test
    public void testRemoveMatchingWithNoPatterns() throws Exception {
        Collection<String> values = stringToList("A");
        StringCollectionUtil.removeMatching(values);
        assertTrue(values.contains("A"));
    }

    @Test
    public void testRemoveMatchingWithMatchingPattern() throws Exception {
        Collection<String> values = stringToList("A");
        StringCollectionUtil.removeMatching(values, "A");
        assertTrue(values.isEmpty());
    }

    @Test
    public void testRemoveMatchingWithNoMatchingPattern() throws Exception {
        Collection<String> values = stringToList("A");
        StringCollectionUtil.removeMatching(values, "B");
        assertTrue(values.contains("A"));
    }

    private List<String> stringToList(String... values) {
        List<String> result = new ArrayList<String>(values.length);
        result.addAll(Arrays.asList(values));
        return result;
    }

}
