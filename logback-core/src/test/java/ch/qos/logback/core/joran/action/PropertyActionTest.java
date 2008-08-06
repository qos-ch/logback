package ch.qos.logback.core.joran.action;

import java.util.Iterator;

import junit.framework.TestCase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.util.Constants;

public class PropertyActionTest extends TestCase {

  Context context;
  InterpretationContext ec;
  SubstitutionPropertyAction spAction;
  DummyAttributes atts = new DummyAttributes();
  
  @Override
  protected void setUp() throws Exception {
    context = new ContextBase();
    ec = new InterpretationContext(context, null);
    spAction = new SubstitutionPropertyAction();
    spAction.setContext(context);
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    context = null; 
    spAction = null;
    atts = null;
    super.tearDown();
  }
  
  public void testBegin() {
    atts.setValue("name", "v1");
    atts.setValue("value", "work");
    spAction.begin(ec, null, atts);
    assertEquals("work", ec.getSubstitutionProperty("v1"));
  }
  
  public void testBeginNoValue() {
    atts.setValue("name", "v1");
    spAction.begin(ec, null, atts);
    assertEquals(1, context.getStatusManager().getCount());
    assertTrue(checkError());
  }

  public void testBeginNoName() {
    atts.setValue("value", "v1");
    spAction.begin(ec, null, atts);
    assertEquals(1, context.getStatusManager().getCount());
    assertTrue(checkError());
  }
  
  public void testBeginNothing() {
    spAction.begin(ec, null, atts);
    assertEquals(1, context.getStatusManager().getCount());
    assertTrue(checkError());
  } 
  
  public void testFileNotLoaded() {
    atts.setValue("file", "toto");
    atts.setValue("value", "work");
    spAction.begin(ec, null, atts);
    assertEquals(1, context.getStatusManager().getCount());
    assertTrue(checkError());
  }
  
  public void testLoadFile() {
    atts.setValue("file", Constants.TEST_DIR_PREFIX + "input/joran/propertyActionTest.properties");
    spAction.begin(ec, null, atts);
    assertEquals("tata", ec.getSubstitutionProperty("v1"));
    assertEquals("toto", ec.getSubstitutionProperty("v2"));
  }
  
  public void testLoadNotPossible() {
    atts.setValue("file", "toto");
    spAction.begin(ec, null, atts);
    assertEquals(2, context.getStatusManager().getCount());
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
    boolean result1 = "Could not read properties file [toto].".equals(es1.getMessage());
    ErrorStatus es2 = (ErrorStatus)it.next();
    boolean result2 = "Ignoring configuration file [toto].".equals(es2.getMessage());
    return result1 && result2;
  }
}
