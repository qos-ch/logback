package ch.qos.logback.classic.spi;

import java.util.Comparator;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class LoggerComparator implements Comparator<Logger> {

  public int compare(Logger l1, Logger l2) {
    if (l1.getName().equals(LoggerContext.ROOT_NAME)) {
      return -1;
    }
    return l1.getName().compareTo(l2.getName());
  }

}
