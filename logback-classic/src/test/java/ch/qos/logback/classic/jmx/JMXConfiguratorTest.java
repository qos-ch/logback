package ch.qos.logback.classic.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.testUtil.RandomUtil;

public class JMXConfiguratorTest {

  static MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
  LoggerContext lc = new LoggerContext();
  Logger testLogger  = lc.getLogger(this.getClass());

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
    assertFalse("old configurator should be absent", listenerList
        .contains(jmxConfigurator0));
    assertTrue("new configurator should be present", listenerList
        .contains(jmxConfigurator1));

    // StatusPrinter.print(lc);
  }

  @Test
  public void getLoggerLevel_LBCLASSIC_78() {
    String objectNameAsStr = "ch.qos"+diff + ":Name=" + lc.getName()
        + ",Type=" + this.getClass().getName();

    ObjectName on = MBeanUtil.string2ObjectName(lc, this, objectNameAsStr);
    JMXConfigurator configurator = new JMXConfigurator(lc, mbs, on);
    assertEquals("", configurator.getLoggerLevel(testLogger.getName()));
    MBeanUtil.unregister(lc, mbs, on, this);
  }

  
  @Test
  public void setLoggerLevel_LBCLASSIC_79() {
    String objectNameAsStr = "ch.qos"+diff + ":Name=" + lc.getName()
        + ",Type=" + this.getClass().getName();

    ObjectName on = MBeanUtil.string2ObjectName(lc, this, objectNameAsStr);
    JMXConfigurator configurator = new JMXConfigurator(lc, mbs, on);
    configurator.setLoggerLevel(testLogger.getName(), "DEBUG");
    assertEquals(Level.DEBUG,  testLogger.getLevel());
    
    configurator.setLoggerLevel(testLogger.getName(), "null");
    assertNull(testLogger.getLevel());
       
    MBeanUtil.unregister(lc, mbs, on, this);
  }

}
