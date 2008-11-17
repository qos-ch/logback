package ch.qos.logback.core.rolling;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.appender.AbstractAppenderTest;
import ch.qos.logback.core.layout.DummyLayout;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;

public class RollingFileAppenderTest extends AbstractAppenderTest<Object> {

  RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
  Context context = new ContextBase();

  TimeBasedRollingPolicy<Object> tbrp = new TimeBasedRollingPolicy<Object>();

  @Before
  public void setUp() throws Exception {
    rfa.setLayout(new DummyLayout<Object>());
    rfa.setName("test");
    rfa.setRollingPolicy(tbrp);
    
    tbrp.setContext(context);
    tbrp.setParent(rfa);
  }

  @After
  public void tearDown() throws Exception {
  }

  
  @Override
  protected AppenderBase<Object> getAppender() {
    return rfa;
  }

  @Override
  protected AppenderBase<Object> getConfiguredAppender() {
    rfa.setContext(context);

    tbrp.setFileNamePattern("toto-%d.log");
    tbrp.start();
    
    rfa.start();
    return rfa;
  }


  @Test
  public void testPrudentModeLogicalImplications() {
    tbrp.setFileNamePattern("toto-%d.log");
    tbrp.start();
    
    rfa.setContext(context);
    // prudent mode will force "file" property to be null
    rfa.setFile("some non null value");
    rfa.setAppend(false);
    rfa.setImmediateFlush(false);
    rfa.setBufferedIO(true);
    rfa.setPrudent(true);
    rfa.start();

    assertTrue(rfa.getImmediateFlush());
    assertTrue(rfa.isAppend());
    assertFalse(rfa.isBufferedIO());
    assertNull(rfa.rawFileProperty());
    assertTrue(rfa.isStarted());
  }

  
  @Test
  public void testPrudentModeLogicalImplicationsOnCompression() {
    tbrp.setFileNamePattern("toto-%d.log.zip");
    tbrp.start();

    rfa.setContext(context);
    rfa.setAppend(false);
    rfa.setImmediateFlush(false);
    rfa.setBufferedIO(true);
    rfa.setPrudent(true);
    rfa.start();

    StatusManager sm = context.getStatusManager();
    assertFalse(rfa.isStarted());
    assertEquals(Status.ERROR, sm.getLevel());
  }
  
  
}
