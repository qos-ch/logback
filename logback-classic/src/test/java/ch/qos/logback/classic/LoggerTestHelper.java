package ch.qos.logback.classic;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import junit.framework.*;

public class LoggerTestHelper extends TestCase {


  static void assertNameEquals(Logger logger, String name)  {
    assertNotNull(logger);
    assertEquals(name, logger.getName());
  }
  static void assertLevels(Level level, Logger logger, Level effectiveLevel)  {
    if(level == null) {
    assertNull(logger.getLevel());
    } else {
      assertEquals(level, logger.getLevel());
    }
    assertEquals(effectiveLevel, logger.getEffectiveLevel());
  }



}