package ch.qos.logback.classic.selector;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.ClassicGlobal;
import ch.qos.logback.classic.net.mock.MockInitialContext;
import ch.qos.logback.classic.net.mock.MockInitialContextFactory;
import ch.qos.logback.classic.selector.servlet.ContextDetachingSCL;

public class ContextDetachingSCLTest extends TestCase {
  
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
  
  public void testDetach() {
    ContextJNDISelector selector = (ContextJNDISelector) LoggerFactory.getContextSelector();
    listener.contextDestroyed(null);
    
    assertEquals(0, selector.getCount());
  }
  
  public void testDetachWithMissingContext() {
    MockInitialContext mic = MockInitialContextFactory.getContext();
    mic.map.put(ClassicGlobal.JNDI_CONTEXT_NAME, "tata");
    
    ContextJNDISelector selector = (ContextJNDISelector) LoggerFactory.getContextSelector();
    listener.contextDestroyed(null);
    
    assertEquals(1, selector.getCount());
  }
  
}
