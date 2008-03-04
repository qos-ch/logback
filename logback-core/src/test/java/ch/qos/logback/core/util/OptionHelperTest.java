package ch.qos.logback.core.util;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;

import junit.framework.TestCase;

public class OptionHelperTest extends TestCase {

  String text = "Testing ${v1} variable substitution ${v2}";
  String expected = "Testing if variable substitution works";
  Context context = new ContextBase();
  Map<String, String> secondaryMap;
  
  
  
  @Override
  protected void setUp() throws Exception {
    secondaryMap = new HashMap<String, String>();
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testLiteral() {
    String noSubst = "hello world";
    String result = OptionHelper.substVars(noSubst, context);
    assertEquals(noSubst, result);
  }

  
  public void testUndefinedValues() {
    String withUndefinedValues = "${axyz}";
    
    String result = OptionHelper.substVars(withUndefinedValues, context);
    assertEquals("axyz"+OptionHelper._IS_UNDEFINED, result);
  }
  
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
  
  public void testSubstVarsContextOnly() {
    context.putProperty("v1", "if");
    context.putProperty("v2", "works");
    
    String result = OptionHelper.substVars(text, context);
    assertEquals(expected, result); 
  }
  
  
  public void testSubstVarsSystemProperties() { 
    System.setProperty("v1", "if");
    System.setProperty("v2", "works");
    
    String result = OptionHelper.substVars(text, context);
    assertEquals(expected, result); 
    
    System.clearProperty("v1");
    System.clearProperty("v2");
  }
  
  public void testSubstVarsWithDefault() {   
    context.putProperty("v1", "if");
    String textWithDefault = "Testing ${v1} variable substitution ${v2:-toto}";
    String resultWithDefault = "Testing if variable substitution toto";
    
    String result = OptionHelper.substVars(textWithDefault, context);
    assertEquals(resultWithDefault, result); 
  }
  
  public void testSubstVarsRecursive() {
    context.putProperty("v1", "if");
    context.putProperty("v2", "${v3}");
    context.putProperty("v3", "works");
    
    String result = OptionHelper.substVars(text, context);
    assertEquals(expected, result); 
  }
  
}
