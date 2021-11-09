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
package ch.qos.logback.core.joran.spi;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test pattern manipulation code.
 *
 * @author Ceki Gulcu
 */
public class ElementSelectorTest {

	@Test
	public void test1() {
		final ElementSelector p = new ElementSelector("a");
		assertEquals(1, p.size());
		assertEquals("a", p.peekLast());
		assertEquals("a", p.get(0));
	}

	@Test
	public void testSuffix() {
		final ElementSelector p = new ElementSelector("a/");
		assertEquals(1, p.size());
		assertEquals("a", p.peekLast());
		assertEquals("a", p.get(0));
	}

	@Test
	public void test2() {
		final ElementSelector p = new ElementSelector("a/b");
		assertEquals(2, p.size());
		assertEquals("b", p.peekLast());
		assertEquals("a", p.get(0));
		assertEquals("b", p.get(1));
	}

	@Test
	public void test3() {
		final ElementSelector p = new ElementSelector("a123/b1234/cvvsdf");
		assertEquals(3, p.size());
		assertEquals("a123", p.get(0));
		assertEquals("b1234", p.get(1));
		assertEquals("cvvsdf", p.get(2));
	}

	@Test
	public void test4() {
		final ElementSelector p = new ElementSelector("/a123/b1234/cvvsdf");
		assertEquals(3, p.size());
		assertEquals("a123", p.get(0));
		assertEquals("b1234", p.get(1));
		assertEquals("cvvsdf", p.get(2));
	}

	@Test
	public void test5() {
		final ElementSelector p = new ElementSelector("//a");
		assertEquals(1, p.size());
		assertEquals("a", p.get(0));
	}

	@Test
	public void test6() {
		final ElementSelector p = new ElementSelector("//a//b");
		assertEquals(2, p.size());
		assertEquals("a", p.get(0));
		assertEquals("b", p.get(1));
	}

	// test tail matching
	@Test
	public void testTailMatch() {
		{
			final ElementPath p = new ElementPath("/a/b");
			final ElementSelector ruleElementSelector = new ElementSelector("*");
			assertEquals(0, ruleElementSelector.getTailMatchLength(p));
		}

		{
			final ElementPath p = new ElementPath("/a");
			final ElementSelector ruleElementSelector = new ElementSelector("*/a");
			assertEquals(1, ruleElementSelector.getTailMatchLength(p));
		}

		{
			final ElementPath p = new ElementPath("/A");
			final ElementSelector ruleElementSelector = new ElementSelector("*/a");
			assertEquals(1, ruleElementSelector.getTailMatchLength(p));
		}

		{
			final ElementPath p = new ElementPath("/a");
			final ElementSelector ruleElementSelector = new ElementSelector("*/A");
			assertEquals(1, ruleElementSelector.getTailMatchLength(p));
		}

		{
			final ElementPath p = new ElementPath("/a/b");
			final ElementSelector ruleElementSelector = new ElementSelector("*/b");
			assertEquals(1, ruleElementSelector.getTailMatchLength(p));
		}

		{
			final ElementPath p = new ElementPath("/a/B");
			final ElementSelector ruleElementSelector = new ElementSelector("*/b");
			assertEquals(1, ruleElementSelector.getTailMatchLength(p));
		}

		{
			final ElementPath p = new ElementPath("/a/b/c");
			final ElementSelector ruleElementSelector = new ElementSelector("*/b/c");
			assertEquals(2, ruleElementSelector.getTailMatchLength(p));
		}
	}

	// test prefix matching
	@Test
	public void testPrefixMatch() {
		{
			final ElementPath p = new ElementPath("/a/b");
			final ElementSelector ruleElementSelector = new ElementSelector("/x/*");
			assertEquals(0, ruleElementSelector.getPrefixMatchLength(p));
		}

		{
			final ElementPath p = new ElementPath("/a");
			final ElementSelector ruleElementSelector = new ElementSelector("/x/*");
			assertEquals(0, ruleElementSelector.getPrefixMatchLength(p));
		}

		{
			final ElementPath p = new ElementPath("/a/b");
			final ElementSelector ruleElementSelector = new ElementSelector("/a/*");
			assertEquals(1, ruleElementSelector.getPrefixMatchLength(p));
		}

		{
			final ElementPath p = new ElementPath("/a/b");
			final ElementSelector ruleElementSelector = new ElementSelector("/A/*");
			assertEquals(1, ruleElementSelector.getPrefixMatchLength(p));
		}

		{
			final ElementPath p = new ElementPath("/A/b");
			final ElementSelector ruleElementSelector = new ElementSelector("/a/*");
			assertEquals(1, ruleElementSelector.getPrefixMatchLength(p));
		}

		{
			final ElementPath p = new ElementPath("/a/b");
			final ElementSelector ruleElementSelector = new ElementSelector("/a/b/*");
			assertEquals(2, ruleElementSelector.getPrefixMatchLength(p));
		}

		{
			final ElementPath p = new ElementPath("/a/b");
			final ElementSelector ruleElementSelector = new ElementSelector("/*");
			assertEquals(0, ruleElementSelector.getPrefixMatchLength(p));
		}
	}

}
