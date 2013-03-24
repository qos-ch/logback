/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.net.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

import ch.qos.logback.core.status.StatusUtil;

/**
 * Unit tests for {@link ServerContext}.
 *
 * @author Carl Harris
 */
public class ServerContextTest {

  private static final String TEST_ERROR_MESSAGE = "test error message";
  
  private ByteArrayOutputStream out = new ByteArrayOutputStream();
  private ByteArrayOutputStream err = new ByteArrayOutputStream();
  
  private ServerContext context = new ServerContext(
      new PrintStream(out), new PrintStream(err));
  
  @Test
  public void testAddError() {
    assertFalse(context.hasErrors());
    context.addError(TEST_ERROR_MESSAGE);
    assertTrue(context.hasErrors());
    assertEquals(0, out.toByteArray().length);
    assertEquals(TEST_ERROR_MESSAGE + "\n", new String(err.toByteArray()));
  }

  @Test
  public void testAddWarning() {
    assertFalse(context.hasErrors());
    StatusUtil.addWarn(context, this, TEST_ERROR_MESSAGE);
    assertFalse(context.hasErrors());
    assertEquals(0, err.toByteArray().length);
    assertEquals(TEST_ERROR_MESSAGE + "\n", new String(out.toByteArray()));
  }

  @Test
  public void testAddInfo() {
    assertFalse(context.hasErrors());
    StatusUtil.addInfo(context, this, TEST_ERROR_MESSAGE);
    assertFalse(context.hasErrors());
    assertEquals(0, out.toByteArray().length);
    assertEquals(0, err.toByteArray().length);
  }

}
