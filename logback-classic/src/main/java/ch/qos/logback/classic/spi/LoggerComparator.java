package ch.qos.logback.classic.spi;

import java.util.Comparator;

import ch.qos.logback.classic.Logger;

public class LoggerComparator implements Comparator<Logger> {

  public int compare(Logger l1, Logger l2) {
    if (l1.getName().equals(l2.getName())) {
      return 0;
    }
    if (l1.getName().equals(Logger.ROOT_LOGGER_NAME)) {
      return -1;
    }
    if (l2.getName().equals(Logger.ROOT_LOGGER_NAME)) {
      return 1;
    }
    return l1.getName().compareTo(l2.getName());
  }

}
