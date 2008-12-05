package ch.qos.logback.core.joran.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.util.Constants;
import ch.qos.logback.core.util.StatusPrinter;

public class PropertyActionTest  {

  Context context;
  InterpretationContext ec;
  PropertyAction spAction;
  DummyAttributes atts = new DummyAttributes();
  
  @Before
  public void setUp() throws Exception {
    context = new ContextBase();
    ec = new InterpretationContext(context, null);
    spAction = new PropertyAction();
    spAction.setContext(context);
  }

  @After
  public void tearDown() throws Exception {
    context = null; 
    spAction = null;
    atts = null;
  }
  
  @Test
  public void nameValuePair() {
    atts.setValue("name", "v1");
    atts.setValue("value", "work");
    spAction.begin(ec, null, atts);
    assertEquals("work", ec.getSubstitutionProperty("v1"));
  }
  
  @Test
  public void nameValuePairWithPrerequisiteSubsitution() {
    context.putProperty("w", "wor");
    atts.setValue("name", "v1");
    atts.setValue("value", "${w}k");
    spAction.begin(ec, null, atts);
    assertEquals("work", ec.getSubstitutionProperty("v1"));
  }
  
  @Test
  public void noValue() {
    atts.setValue("name", "v1");
    spAction.begin(ec, null, atts);
    assertEquals(1, context.getStatusManager().getCount());
    assertTrue(checkError());
  }

  @Test
  public void noName() {
    atts.setValue("value", "v1");
    spAction.begin(ec, null, atts);
    assertEquals(1, context.getStatusManager().getCount());
    assertTrue(checkError());
  }
  
  @Test
  public void noAttributes() {
    spAction.begin(ec, null, atts);
    assertEquals(1, context.getStatusManager().getCount());
    assertTrue(checkError());
    StatusPrinter.print(context);
  } 
  
  @Test
  public void testFileNotLoaded() {
    atts.setValue("file", "toto");
    atts.setValue("value", "work");
    spAction.begin(ec, null, atts);
    assertEquals(1, context.getStatusManager().getCount());
    assertTrue(checkError());
  }
  
  @Test
  public void testLoadFileWithPrerequisiteSubsitution() {
    context.putProperty("STEM", Constants.TEST_DIR_PREFIX + "input/joran");
    atts.setValue("file", "${STEM}/propertyActionTest.properties");
    spAction.begin(ec, null, atts);
    assertEquals("tata", ec.getSubstitutionProperty("v1"));
    assertEquals("toto", ec.getSubstitutionProperty("v2"));
  }

  @Test
  public void testLoadFile() {
    atts.setValue("file", Constants.TEST_DIR_PREFIX + "input/joran/propertyActionTest.properties");
    spAction.begin(ec, null, atts);
    assertEquals("tata", ec.getSubstitutionProperty("v1"));
    assertEquals("toto", ec.getSubstitutionProperty("v2"));
  }

  @Test
  public void testLoadResource() {
    atts.setValue("resource", "asResource/joran/propertyActionTest.properties");
    spAction.begin(ec, null, atts);
    assertEquals("tata", ec.getSubstitutionProperty("r1"));
    assertEquals("toto", ec.getSubstitutionProperty("r2"));
  }
  
  @Test
  public void testLoadResourceWithPrerequisiteSubsitution() {
    context.putProperty("STEM", "asResource/joran");
    atts.setValue("resource", "${STEM}/propertyActionTest.properties");
    spAction.begin(ec, null, atts);
    assertEquals("tata", ec.getSubstitutionProperty("r1"));
    assertEquals("toto", ec.getSubstitutionProperty("r2"));
  }
  
  @Test
  public void testLoadNotPossible() {
    atts.setValue("file", "toto");
    spAction.begin(ec, null, atts);
    assertEquals(1, context.getStatusManager().getCount());
    assertTrue(checkFileErrors());
  }
  
  private boolean checkError() {
    Iterator it = context.getStatusManager().getCopyOfStatusList().iterator();
    ErrorStatus es = (ErrorStatus)it.next();
    return PropertyAction.INVALID_ATTRIBUTES.equals(es.getMessage());
  }
  
  private boolean checkFileErrors() {
    Iterator it = context.getStatusManager().getCopyOfStatusList().iterator();
    ErrorStatus es1 = (ErrorStatus)it.next();
    return "Could not read properties file [toto].".equals(es1.getMessage());
  }
}
