/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 * <p>
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 * <p>
 * or (per the licensee's choosing)
 * <p>
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.status;

import java.util.Iterator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StatusBaseTest {


    @Test
    public void testAddStatus() {
        {
            InfoStatus status = new InfoStatus("testing", this);
            status.add(new ErrorStatus("error", this));
            Iterator<Status> it = status.iterator();
            Assertions.assertTrue(it.hasNext(), "No status was added");
            Assertions.assertTrue(status.hasChildren(), "hasChilden method reported wrong result");
        }
        {
            InfoStatus status = new InfoStatus("testing", this);
            try {
                status.add(null);
                Assertions.fail("method should have thrown an Exception");
            } catch (NullPointerException ex) {
            }
        }
    }

    @Test
    public void testRemoveStatus() {
        {
            InfoStatus status = new InfoStatus("testing", this);
            ErrorStatus error = new ErrorStatus("error", this);
            status.add(error);
            boolean result = status.remove(error);
            Iterator<Status> it = status.iterator();
            Assertions.assertTrue(result, "Remove failed");
            Assertions.assertFalse(it.hasNext(), "No status was removed");
            Assertions.assertFalse(status.hasChildren(), "hasChilden method reported wrong result");
        }
        {
            InfoStatus status = new InfoStatus("testing", this);
            ErrorStatus error = new ErrorStatus("error", this);
            status.add(error);
            boolean result = status.remove(null);
            Assertions.assertFalse(result, "Remove result was not false");
        }
    }

    public void testEffectiveLevel() {
        {
            // effective level = 0 level deep
            ErrorStatus status = new ErrorStatus("error", this);
            WarnStatus warn = new WarnStatus("warning", this);
            status.add(warn);
            Assertions.assertEquals(status.getEffectiveLevel(), Status.ERROR, "effective level misevaluated");
        }

        {
            // effective level = 1 level deep
            InfoStatus status = new InfoStatus("info", this);
            WarnStatus warn = new WarnStatus("warning", this);
            status.add(warn);
            Assertions.assertEquals(status.getEffectiveLevel(), Status.WARN, "effective level misevaluated");
        }

        {
            // effective level = 2 levels deep
            InfoStatus status = new InfoStatus("info", this);
            WarnStatus warn = new WarnStatus("warning", this);
            ErrorStatus error = new ErrorStatus("error", this);
            status.add(warn);
            warn.add(error);
            Assertions.assertEquals(status.getEffectiveLevel(), Status.ERROR, "effective level misevaluated");
        }
    }

}
