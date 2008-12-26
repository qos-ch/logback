package ch.qos.logback.classic.selector;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import ch.qos.logback.classic.ClassicGlobal;
import ch.qos.logback.classic.selector.servlet.ContextDetachingSCL;
import ch.qos.logback.classic.util.MockInitialContext;
import ch.qos.logback.classic.util.MockInitialContextFactory;

public class ContextDetachingSCLTest  {
  
  static String INITIAL_CONTEXT_KEY = "java.naming.factory.initial";

  ContextDetachingSCL listener;
  
  @Before
  public void setUp() throws Exception {
    System.setProperty(ClassicGlobal.LOGBACK_CONTEXT_SELECTOR, "JNDI");
    //LoggerFactory.setup();
    
    listener = new ContextDetachingSCL();
    
    MockInitialContextFactory.initialize();
    MockInitialContext mic = MockInitialContextFactory.getContext();
    mic.map.put(ClassicGlobal.JNDI_CONTEXT_NAME, "toto");
    
    //The property must be set after we setup the Mock
    System.setProperty(INITIAL_CONTEXT_KEY, MockInitialContextFactory.class.getName());
    
    //this call will create the context "toto"
    LoggerFactory.getLogger(ContextDetachingSCLTest.class);

  }

  @After
  public void tearDown() throws Exception {
    System.clearProperty(INITIAL_CONTEXT_KEY);
  }

  @Test
  public void testDetach() {
    ContextJNDISelector selector = (ContextJNDISelector) StaticLoggerBinder.getSingleton().getContextSelector();
    listener.contextDestroyed(null);
    assertEquals(0, selector.getCount());
  }
  

  @Test
  public void testDetachWithMissingContext() {
    MockInitialContext mic = MockInitialContextFactory.getContext();
    mic.map.put(ClassicGlobal.JNDI_CONTEXT_NAME, "tata");
    ContextJNDISelector selector = (ContextJNDISelector) StaticLoggerBinder.getSingleton().getContextSelector();
    assertEquals("tata", selector.getLoggerContext().getName());

    mic.map.put(ClassicGlobal.JNDI_CONTEXT_NAME, "titi");
    assertEquals("titi", selector.getLoggerContext().getName());
    listener.contextDestroyed(null);

    assertEquals(2, selector.getCount());
  }
  
}
