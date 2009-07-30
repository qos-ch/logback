package ch.qos.logback.core.joran.spi;

public class NoAutoStartUtil {

  /**
   * Returns true if the class of the object 'o' passed as parameter is *not*
   * marked with the NoAutoStart annotation. Return true otherwise.
   * 
   * @param o
   * @return true for classes not marked with the NoAutoStart annotation
   */
  static public boolean notMarkedWithNoAutoStart(Object o) {
    if (o == null) {
      return false;
    }
    Class<?> clazz = o.getClass();
    NoAutoStart a = clazz.getAnnotation(NoAutoStart.class);
    return a == null;
  }

}
