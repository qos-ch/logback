package ch.qos.logback.classic.spi;

import junit.framework.TestCase;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.BasicContextListener.UpdateType;

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
    assertEquals(UpdateType.RESET, listener.updateType);
    assertEquals(listener.context, context);
  }

  public void testNotifyOnStopResistant() {
    listener.setResetResistant(true);
    context.stop();
    assertEquals(UpdateType.STOP, listener.updateType);
    assertEquals(listener.context, context);
  }

  public void testNotifyOnStopNotResistant() {
    context.stop();
    assertEquals(UpdateType.RESET, listener.updateType);
    assertEquals(listener.context, context);
  }

  
  public void testNotifyOnStart() {
    context.start();
    assertEquals(UpdateType.START, listener.updateType);
    assertEquals(listener.context, context);
  }
}
