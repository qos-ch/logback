package ch.qos.logback.classic.util;

public class TeztHelper {

  
  static public Throwable makeNestedException(int level) {
    if (level == 0) {
      return new Exception("nesting level=" + level);
    }
    Throwable cause = makeNestedException(level - 1);
    return new Exception("nesting level =" + level, cause);
  }
}
