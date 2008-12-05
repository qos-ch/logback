package ch.qos.logback.classic.spi;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;


public class LoggerComparatorTest {

  LoggerComparator comparator = new LoggerComparator();
  LoggerContext lc = new LoggerContext();

  Logger root = lc.getLogger("root");

  Logger a = lc.getLogger("a");
  Logger b = lc.getLogger("b");

  @Before
  public void setUp() throws Exception {
  
  }

  
  
  @Test
  public void testSmoke() {
    assertEquals(0, comparator.compare(a, a));
    assertEquals(-1, comparator.compare(a, b));
    assertEquals(1, comparator.compare(b, a));
    assertEquals(-1, comparator.compare(root, a));
    // following two tests failed before bug #127 was fixed
    assertEquals(1, comparator.compare(a, root));
    assertEquals(0, comparator.compare(root, root));
  }
}
