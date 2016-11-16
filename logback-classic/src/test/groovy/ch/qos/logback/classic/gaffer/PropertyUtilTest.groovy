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
package ch.qos.logback.classic.gaffer

import org.junit.Test
import static junit.framework.Assert.assertEquals

/**
 * @author Ceki G&uuml;c&uuml;
 */
class PropertyUtilTest {


  @Test
  void empty() {
    assertEquals("", PropertyUtil.upperCaseFirstLetter(""));
    assertEquals(null, PropertyUtil.upperCaseFirstLetter(null));
  }



  @Test
  void smoke() {
    assertEquals("Hello", PropertyUtil.upperCaseFirstLetter("hello"));
  }


}
