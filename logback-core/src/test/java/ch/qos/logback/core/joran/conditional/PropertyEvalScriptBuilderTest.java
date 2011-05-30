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
package ch.qos.logback.core.joran.conditional;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.testUtil.RandomUtil;

public class PropertyEvalScriptBuilderTest {

  
  Context context = new ContextBase();
  PropertyEvalScriptBuilder pesb = new PropertyEvalScriptBuilder();
  int diff = RandomUtil.getPositiveInt();
  
  @Before
  public void setUp() {
    context.setName("c"+diff);
    pesb.setContext(context);
  }
  
  @Test
  public void existing() throws Exception {
    String k = "ka"+diff;
    context.putProperty("ka"+diff, "va");
    Condition condition = pesb.build("p(\""+k+"\").contains(\"va\")");
    assertNotNull(condition);
    assertTrue(condition.evaluate());
  }

  @Test
  public void isNullForExisting() throws Exception {
    String k = "ka"+diff;
    context.putProperty("ka"+diff, "va");
    Condition condition = pesb.build("isNull(\""+k+"\")");
    assertNotNull(condition);
    assertFalse(condition.evaluate());
  }

  
  @Test
  public void inexistentProperty() throws Exception {
    String k = "ka"+diff;
    Condition condition = pesb.build("p(\""+k+"\").contains(\"va\")");
    assertNotNull(condition);
    assertFalse(condition.evaluate());
  }

  @Test
  public void isNullForInexistent() throws Exception {
    String k = "ka"+diff;
    Condition condition = pesb.build("isNull(\""+k+"\")");
    assertNotNull(condition);
    assertTrue(condition.evaluate());
  }

  @Test
  public void nameOK() throws Exception {
    Condition condition = pesb.build("name.contains(\""+context.getName()+"\")");
    assertNotNull(condition);
    assertTrue(condition.evaluate());
  }

  @Test
  public void wrongName() throws Exception {
    Condition condition = pesb.build("name.contains(\"x\")");
    assertNotNull(condition);
    assertFalse(condition.evaluate());
  }
  
}
