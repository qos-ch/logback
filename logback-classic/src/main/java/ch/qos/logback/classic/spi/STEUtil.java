package ch.qos.logback.classic.spi;

public class STEUtil {

  
  static int UNUSED_findNumberOfCommonFrames(StackTraceElement[] steArray,
      StackTraceElement[] otherSTEArray) {
    if (otherSTEArray == null) {
      return 0;
    }

    int steIndex = steArray.length - 1;
    int parentIndex = otherSTEArray.length - 1;
    int count = 0;
    while (steIndex >= 0 && parentIndex >= 0) {
      if (steArray[steIndex].equals(otherSTEArray[parentIndex])) {
        count++;
      } else {
        break;
      }
      steIndex--;
      parentIndex--;
    }
    return count;
  }
  
  
  static int findNumberOfCommonFrames(StackTraceElement[] steArray,
      StackTraceElementProxy[] otherSTEPArray) {
    if (otherSTEPArray == null) {
      return 0;
    }

    int steIndex = steArray.length - 1;
    int parentIndex = otherSTEPArray.length - 1;
    int count = 0;
    while (steIndex >= 0 && parentIndex >= 0) {
      if (steArray[steIndex].equals(otherSTEPArray[parentIndex].ste)) {
        count++;
      } else {
        break;
      }
      steIndex--;
      parentIndex--;
    }
    return count;
  }
}
