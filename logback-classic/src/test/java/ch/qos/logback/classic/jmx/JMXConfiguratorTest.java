package ch.qos.logback.classic.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusPrinter;

public class JMXConfiguratorTest {

  static MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
  LoggerContext lc = new LoggerContext();
  List<LoggerContextListener> listenerList;
  int diff = RandomUtil.getPositiveInt();

  @Before
  public void setUp() throws Exception {
    lc.setName("context-" + diff);
  }

  @After
  public void tearDown() throws Exception {
    lc.reset();
  }

  @Override
  public String toString() {
    return this.getClass().getName() + "(" + lc.getName() + ")";
  }

  @Test
  public void contextListening() {
    String objectNameAsStr = "ch.qos.logback.toto" + ":Name=" + lc.getName()
        + ",Type=" + this.getClass().getName();
    ObjectName on = MBeanUtil.string2ObjectName(lc, this, objectNameAsStr);

    MBeanUtil.register(lc, on, this);
    listenerList = lc.getCopyOfListenerList();
    assertEquals(1, listenerList.size());
    lc.reset();
    listenerList = lc.getCopyOfListenerList();
    assertEquals(0, listenerList.size());

    MBeanUtil.register(lc, on, this);
    listenerList = lc.getCopyOfListenerList();
    assertEquals(1, listenerList.size());

  }

  @Test
  public void testRemovalOfPreviousIntanceFromTheContextListenerList() {
    String objectNameAsStr = "ch.qos.logback.toto" + ":Name=" + lc.getName()
        + ",Type=" + this.getClass().getName();

    ObjectName on = MBeanUtil.string2ObjectName(lc, this, objectNameAsStr);
    JMXConfigurator jmxConfigurator0 = MBeanUtil.register(lc, on, this);

    listenerList = lc.getCopyOfListenerList();
    assertEquals(1, listenerList.size());
    assertTrue(listenerList.contains(jmxConfigurator0));
    
    JMXConfigurator jmxConfigurator1 = MBeanUtil.register(lc, on, this);
    listenerList = lc.getCopyOfListenerList();
    assertEquals(1, listenerList.size());
    assertFalse("old configurator should be absent", listenerList.contains(jmxConfigurator0));
    assertTrue("new configurator should be present", listenerList.contains(jmxConfigurator1));
    
    StatusPrinter.print(lc);

  }

}
