package ch.qos.logback.classic.selector;

import junit.framework.TestCase;

import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import ch.qos.logback.classic.ClassicGlobal;
import ch.qos.logback.classic.net.mock.MockInitialContext;
import ch.qos.logback.classic.net.mock.MockInitialContextFactory;
import ch.qos.logback.core.Context;

public class ContextJNDISelectorTest extends TestCase {
  
  static String INITIAL_CONTEXT_KEY = "java.naming.factory.initial";

  @Override
  protected void setUp() throws Exception {    
    super.setUp();
    
    System.setProperty(ClassicGlobal.LOGBACK_CONTEXT_SELECTOR, "JNDI");
    StaticLoggerBinder.SINGLETON.initialize();
    
    MockInitialContextFactory.initialize();
    MockInitialContext mic = MockInitialContextFactory.getContext();
    mic.map.put(ClassicGlobal.JNDI_CONTEXT_NAME, "toto");
    
    //The property must be set after we setup the Mock
    System.setProperty(INITIAL_CONTEXT_KEY, MockInitialContextFactory.class.getName());
    
    //this call will create the context "toto"
    LoggerFactory.getLogger(ContextDetachingSCLTest.class);
  }

  @Override
  protected void tearDown() throws Exception {
    System.clearProperty(INITIAL_CONTEXT_KEY);
    super.tearDown();
  }

  public void testGetExistingContext() {
    ContextSelector selector = StaticLoggerBinder.SINGLETON.getContextSelector();
    Context context = selector.getLoggerContext();
    assertEquals("toto", context.getName());
  }
  
  public void testCreateContext() {
    MockInitialContext mic = MockInitialContextFactory.getContext();
    mic.map.put(ClassicGlobal.JNDI_CONTEXT_NAME, "tata");
    
    LoggerFactory.getLogger(ContextDetachingSCLTest.class);
    
    ContextJNDISelector selector = (ContextJNDISelector)StaticLoggerBinder.SINGLETON.getContextSelector();
    Context context = selector.getLoggerContext();
    assertEquals("tata", context.getName());
    assertEquals(1, selector.getCount());
  }
  
  public void testReturnDefaultContext() {
    MockInitialContext mic = MockInitialContextFactory.getContext();
    mic.map.put(ClassicGlobal.JNDI_CONTEXT_NAME, null);

    ContextJNDISelector selector = (ContextJNDISelector)StaticLoggerBinder.SINGLETON.getContextSelector();
    Context context = selector.getLoggerContext();
    
    assertEquals("default", context.getName());    
  }
  
}
