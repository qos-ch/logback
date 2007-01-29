package ch.qos.logback.classic.selector;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.ClassicGlobal;
import ch.qos.logback.classic.net.mock.MockInitialContext;
import ch.qos.logback.classic.net.mock.MockInitialContextFactory;
import ch.qos.logback.classic.selector.servlet.ContextDetachingSCL;
import ch.qos.logback.core.Context;

public class ContextJNDISelectorTest extends TestCase {
  
  static String INITIAL_CONTEXT_KEY = "java.naming.factory.initial";

  ContextDetachingSCL listener;
  
  @Override
  protected void setUp() throws Exception {
    System.setProperty(ClassicGlobal.LOGBACK_CONTEXT_SELECTOR, "JNDI");
    listener = new ContextDetachingSCL();
    
    MockInitialContext mic = MockInitialContextFactory.getContext();
    mic.map.put(ClassicGlobal.JNDI_CONTEXT_NAME, "toto");
    
    //The property must be set after we setup the Mock
    System.setProperty(INITIAL_CONTEXT_KEY, MockInitialContextFactory.class.getName());
    
    //this call will create the context "toto"
    @SuppressWarnings("unused")
    Logger logger = LoggerFactory.getLogger(ContextDetachingSCLTest.class);

    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    System.clearProperty(INITIAL_CONTEXT_KEY);
    super.tearDown();
  }

  public void testGetExistingContext() {
    ContextSelector selector = LoggerFactory.getContextSelector();
    Context context = selector.getLoggerContext();
    assertEquals("toto", context.getName());
  }
  
  public void testCreateContext() {
    MockInitialContext mic = MockInitialContextFactory.getContext();
    mic.map.put(ClassicGlobal.JNDI_CONTEXT_NAME, "tata");
    
    @SuppressWarnings("unused")
    Logger logger = LoggerFactory.getLogger(ContextDetachingSCLTest.class);
    
    ContextJNDISelector selector = (ContextJNDISelector)LoggerFactory.getContextSelector();
    Context context = selector.getLoggerContext();
    assertEquals("tata", context.getName());
    assertEquals(2, selector.getCount());
  }
  
}
