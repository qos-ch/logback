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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ContentTypeUtilTest {

    @Test
    public void smoke() {
        String contextType = "text/html";
        Assertions.assertTrue(ContentTypeUtil.isTextual(contextType));
        Assertions.assertEquals("html", ContentTypeUtil.getSubType(contextType));
    }

    @Test
    public void nullContext() {
        String contextType = null;
        Assertions.assertFalse(ContentTypeUtil.isTextual(contextType));
        Assertions.assertNull(ContentTypeUtil.getSubType(contextType));
    }

    @Test
    public void emptySubtype() {
        String contextType = "text/";
        Assertions.assertTrue(ContentTypeUtil.isTextual(contextType));
        Assertions.assertNull(ContentTypeUtil.getSubType(contextType));
    }
}
