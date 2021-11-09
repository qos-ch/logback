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
package ch.qos.logback.core.status;

import java.util.Iterator;

import junit.framework.TestCase;

public class StatusBaseTest extends TestCase {

	public void testAddStatus() {
		{
			final InfoStatus status = new InfoStatus("testing", this);
			status.add(new ErrorStatus("error", this));
			final Iterator<Status> it = status.iterator();
			assertTrue("No status was added", it.hasNext());
			assertTrue("hasChilden method reported wrong result", status.hasChildren());
		}
		{
			final InfoStatus status = new InfoStatus("testing", this);
			try {
				status.add(null);
				fail("method should have thrown an Exception");
			} catch (final NullPointerException ex) {
			}
		}
	}

	public void testRemoveStatus() {
		{
			final InfoStatus status = new InfoStatus("testing", this);
			final ErrorStatus error = new ErrorStatus("error", this);
			status.add(error);
			final boolean result = status.remove(error);
			final Iterator<Status> it = status.iterator();
			assertTrue("Remove failed", result);
			assertFalse("No status was removed", it.hasNext());
			assertFalse("hasChilden method reported wrong result", status.hasChildren());
		}
		{
			final InfoStatus status = new InfoStatus("testing", this);
			final ErrorStatus error = new ErrorStatus("error", this);
			status.add(error);
			final boolean result = status.remove(null);
			assertFalse("Remove result was not false", result);
		}
	}

	public void testEffectiveLevel() {
		{
			// effective level = 0 level deep
			final ErrorStatus status = new ErrorStatus("error", this);
			final WarnStatus warn = new WarnStatus("warning", this);
			status.add(warn);
			assertEquals("effective level misevaluated", status.getEffectiveLevel(), Status.ERROR);
		}

		{
			// effective level = 1 level deep
			final InfoStatus status = new InfoStatus("info", this);
			final WarnStatus warn = new WarnStatus("warning", this);
			status.add(warn);
			assertEquals("effective level misevaluated", status.getEffectiveLevel(), Status.WARN);
		}

		{
			// effective level = 2 levels deep
			final InfoStatus status = new InfoStatus("info", this);
			final WarnStatus warn = new WarnStatus("warning", this);
			final ErrorStatus error = new ErrorStatus("error", this);
			status.add(warn);
			warn.add(error);
			assertEquals("effective level misevaluated", status.getEffectiveLevel(), Status.ERROR);
		}
	}

}
