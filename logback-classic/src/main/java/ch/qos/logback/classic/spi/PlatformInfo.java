package ch.qos.logback.classic.spi;

/**
 * This class provides information about the runtime platform.
 *
 * @author Ceki Gulcu
 * */
public class PlatformInfo {

  private static final int UNINITIALIZED = -1;

  private static int hasJMXObjectName = UNINITIALIZED;
  
  public static boolean hasJMXObjectName() {
    if (hasJMXObjectName == UNINITIALIZED) {
      try {
        Class.forName("javax.management.ObjectName");
        hasJMXObjectName = 1;
      } catch (Throwable e) {
        hasJMXObjectName = 0;
      }
    }
    return (hasJMXObjectName == 1);
  }
}
