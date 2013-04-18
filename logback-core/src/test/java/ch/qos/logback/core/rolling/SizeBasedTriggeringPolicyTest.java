/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.rolling;

import junit.framework.TestCase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;

public class SizeBasedTriggeringPolicyTest extends TestCase {

  public void testStringToLong() {
    Context context = new ContextBase();
    SizeBasedTriggeringPolicy policy = new SizeBasedTriggeringPolicy();
    policy.setContext(context);

    Long result;

    {
      result = policy.toFileSize("123");
      assertEquals(new Long("123"), result);
    }
    {
      result = policy.toFileSize("123KB");
      // = 123 * 1024
      assertEquals(new Long("125952"), result);
    }
    {
      result = policy.toFileSize("123MB");
      // = 123 * 1024 * 1024
      assertEquals(new Long("128974848"), result);
    }
    {
      result = policy.toFileSize("123GB");
      // = 123 * 1024 * 1024 * 1024
      assertEquals(new Long("132070244352"), result);
    }

    {
      result = policy.toFileSize("123xxxx");
      // = 123 * 1024 * 1024 * 1024
      assertEquals(new Long(SizeBasedTriggeringPolicy.DEFAULT_MAX_FILE_SIZE),
          result);
      assertEquals(2, context.getStatusManager().getCount());
    }

  }
}
