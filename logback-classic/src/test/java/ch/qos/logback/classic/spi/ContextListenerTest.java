package ch.qos.logback.classic.spi;

import junit.framework.TestCase;
import ch.qos.logback.classic.LoggerContext;

public class ContextListenerTest extends TestCase {
  
  LoggerContext context;
  BasicContextListener listener;
  
  public void setUp() throws Exception {
    context = new LoggerContext();
    listener = new BasicContextListener();
    context.addListener(listener);
    super.setUp();
  }
  
  public void testNotifyOnReset() {
    context.reset();
    assertTrue(listener.updated);
    assertEquals(listener.context, context);
  }
  
  public void testNotifyOnStart() {
    context.start();
    assertTrue(listener.updated);
    assertEquals(listener.context, context);
  }
}
