package ch.qos.logback.core.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class OptionHelperTest extends TestCase {

  String text = "Testing ${v1} variable substitution ${v2}";
  String expected = "Testing if variable substitution works";
  Map<String, String> primaryMap;
  Map<String, String> secondaryMap;
  
  
  
  @Override
  protected void setUp() throws Exception {
    primaryMap = new HashMap<String, String>();
    secondaryMap = new HashMap<String, String>();
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    primaryMap = null;
    primaryMap = null;
    super.tearDown();
  }

  public void testSubstVarsNoSubstitution() {
    String noSubst = "testing if it works";
    
    String result = OptionHelper.substVars(noSubst, null, null);
    assertEquals(noSubst, result);
  }
 
  public void testSubstVarsVariableNotClosed() {
    String noSubst = "testing if ${v1 works";
    
    try {
      @SuppressWarnings("unused")
      String result = OptionHelper.substVars(noSubst, null, null);
      fail();
    } catch (IllegalArgumentException e) {
      //ok
    }
  }
  
  public void testSubstVarsPrimaryOnly() {
    primaryMap.put("v1", "if");
    primaryMap.put("v2", "works");
    
    String result = OptionHelper.substVars(text, primaryMap, null);
    assertEquals(expected, result); 
  }
  
  public void testSubstVarsPrimaryAndSecondary() { 
    primaryMap.put("v1", "if");
    secondaryMap.put("v2", "works");
    
    String result = OptionHelper.substVars(text, primaryMap, secondaryMap);
    assertEquals(expected, result); 
  }
  
  
  public void testSubstVarsSystemProperties() { 
    System.setProperty("v1", "if");
    System.setProperty("v2", "works");
    
    String result = OptionHelper.substVars(text, null, null);
    assertEquals(expected, result); 
    
    System.clearProperty("v1");
    System.clearProperty("v2");
  }
  
  public void testSubstVarsWithDefault() {   
    primaryMap.put("v1", "if");
    String textWithDefault = "Testing ${v1} variable substitution ${v2:-toto}";
    String resultWithDefault = "Testing if variable substitution toto";
    
    String result = OptionHelper.substVars(textWithDefault, primaryMap, null);
    assertEquals(resultWithDefault, result); 
  }
  
  public void testSubstVarsRecursive() {
    primaryMap.put("v1", "if");
    primaryMap.put("v2", "${v3}");
    primaryMap.put("v3", "works");
    
    String result = OptionHelper.substVars(text, primaryMap, null);
    assertEquals(expected, result); 
  }
  
}
