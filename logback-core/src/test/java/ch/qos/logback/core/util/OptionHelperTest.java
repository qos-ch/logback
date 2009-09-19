/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;


public class OptionHelperTest  {

  String text = "Testing ${v1} variable substitution ${v2}";
  String expected = "Testing if variable substitution works";
  Context context = new ContextBase();
  Map<String, String> secondaryMap;
  
  
  
  @Before
  public void setUp() throws Exception {
    secondaryMap = new HashMap<String, String>();
  }

  @Test
  public void testLiteral() {
    String noSubst = "hello world";
    String result = OptionHelper.substVars(noSubst, context);
    assertEquals(noSubst, result);
  }

  @Test
  public void testUndefinedValues() {
    String withUndefinedValues = "${axyz}";
    
    String result = OptionHelper.substVars(withUndefinedValues, context);
    assertEquals("axyz"+OptionHelper._IS_UNDEFINED, result);
  }
  
  @Test
  public void testSubstVarsVariableNotClosed() {
    String noSubst = "testing if ${v1 works";
    
    try {
      @SuppressWarnings("unused")
      String result = OptionHelper.substVars(noSubst, context);
      fail();
    } catch (IllegalArgumentException e) {
      //ok
    }
  }
  @Test
  public void testSubstVarsContextOnly() {
    context.putProperty("v1", "if");
    context.putProperty("v2", "works");
    
    String result = OptionHelper.substVars(text, context);
    assertEquals(expected, result); 
  }
  
  @Test
  public void testSubstVarsSystemProperties() { 
    System.setProperty("v1", "if");
    System.setProperty("v2", "works");
    
    String result = OptionHelper.substVars(text, context);
    assertEquals(expected, result); 
    
    System.clearProperty("v1");
    System.clearProperty("v2");
  }
  
  @Test
  public void testSubstVarsWithDefault() {   
    context.putProperty("v1", "if");
    String textWithDefault = "Testing ${v1} variable substitution ${v2:-toto}";
    String resultWithDefault = "Testing if variable substitution toto";
    
    String result = OptionHelper.substVars(textWithDefault, context);
    assertEquals(resultWithDefault, result); 
  }
  
  @Test
  public void testSubstVarsRecursive() {
    context.putProperty("v1", "if");
    context.putProperty("v2", "${v3}");
    context.putProperty("v3", "works");
    
    String result = OptionHelper.substVars(text, context);
    assertEquals(expected, result); 
  }
  
}
