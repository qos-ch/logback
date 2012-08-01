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
package ch.qos.logback.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
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

  String nest1 = "${X1}ABC"; //Y1 => Y1ABC
  String X1 = "Y1";
  String pass1 = "Y1ABC";
  @Test
  public void testNestedVars1() {
    context.putProperty("X1", X1);

    String result = OptionHelper.substVars(nest1, context);
    assertEquals(pass1, result);
  }

  String nest2 = "${${X1}2}ABC"; //Y1 => Y12 => Z34 => Z34ABC
  String Y12 = "Z34";
  String pass2 = "Z34ABC";
  @Test
  public void testNestedVars2() {
    context.putProperty("X1", X1);
    context.putProperty("Y12", Y12);

    String result = OptionHelper.substVars(nest2, context);
    assertEquals(pass2, result);
  }

  String nest3 = "${${${X1}}}ABC"; //Y1 => Z1 => Q1 => Q1ABC
  String Y1 = "Z1";
  String Z1 = "Q1";
  String pass3 = "Q1ABC";
  @Test
  public void testNestedVars3() {
    context.putProperty("X1", X1);
    context.putProperty("Y1", Y1);
    context.putProperty("Z1", Z1);

    String result = OptionHelper.substVars(nest3, context);
    assertEquals(pass3, result);
  }

  String nest4 = "${${X1}${X2}}ABC"; //Y1Y2 => Z1Z2 => Z1Z2ABC
  String X2 = "Y2";
  String Y1Y2 = "Z1Z2";
  String pass4 = "Z1Z2ABC";
  @Test
  public void testNestedVars4() {
    context.putProperty("X1", X1);
    context.putProperty("X2", X2);
    context.putProperty("Y1Y2", Y1Y2);

    String result = OptionHelper.substVars(nest4, context);
    assertEquals(pass4, result);
  }

  String nest5 = "${A${X1}}ABC"; //Y1 => AY1 => BZ2 => BZ2ABC
  String AY1 = "BZ2";
  String pass5 = "BZ2ABC";
  @Test
  public void testNestedVars5() {
    context.putProperty("X1", X1);
    context.putProperty("AY1", AY1);

    String result = OptionHelper.substVars(nest5, context);
    assertEquals(pass5, result);
  }

  String nest6 = "${A${X1}B}CDE"; //Y1 => AY1B => BZ2C => BZ2CCDE
  String AY1B = "BZ2C";
  String pass6 = "BZ2CCDE";
  @Test
  public void testNestedVars6() {
    context.putProperty("X1", X1);
    context.putProperty("AY1B", AY1B);

    String result = OptionHelper.substVars(nest6, context);
    assertEquals(pass6, result);
  }

  String nest7 = "${A${X1}B${X2}}CDE"; //Y1,Y2 => AY1BY2 => BZ2CZ3 => BZ2CZ3CDE
  String AY1BY2 = "BZ2CZ3";
  String pass7 = "BZ2CZ3CDE";
  @Test
  public void testNestedVars7() {
    context.putProperty("X1", X1);
    context.putProperty("X2", X2);
    context.putProperty("AY1BY2", AY1BY2);

    String result = OptionHelper.substVars(nest7, context);
    assertEquals(pass7, result);
  }

  String nest8 = "${A${X1}B${X2}C}DEF"; //Y1,Y2 => AY1BY2C => BZ2CZ3D => BZ2CZ3DDEF
  String AY1BY2C = "BZ2CZ3D";
  String pass8 = "BZ2CZ3DDEF";
  @Test
  public void testNestedVars8() {
    context.putProperty("X1", X1);
    context.putProperty("X2", X2);
    context.putProperty("AY1BY2C", AY1BY2C);

    String result = OptionHelper.substVars(nest8, context);
    assertEquals(pass8, result);
  }

  @Ignore
  @Test
  public void defaultValueReferencingAVariable() {
    context.putProperty("v1", "k1");
    String result = OptionHelper.substVars("${undef:-${v1}}", context);
    assertEquals("k1", result);
  }



  
}
