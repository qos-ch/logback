package ch.qos.logback.classic.issue.lbclassic36;

import java.text.SimpleDateFormat;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.qos.logback.core.issue.RunnableForThrougputComputation;

/**
 * A runnable which behaves differently depending on the desired locking model.
 * 
 * @author Raplh Goers
 * @author Ceki Gulcu
 */
public class SelectiveDateFormattingRunnable extends
    RunnableForThrougputComputation {

  public static final String ISO8601_PATTERN = "yyyy-MM-dd HH:mm:ss,SSS";

  enum FormattingModel {
    SDF, JODA;
  }

  FormattingModel model;
  static long CACHE = 0;

  static SimpleDateFormat SDF = new SimpleDateFormat(ISO8601_PATTERN);
  static final DateTimeFormatter JODA = DateTimeFormat
      .forPattern(ISO8601_PATTERN);

  SelectiveDateFormattingRunnable(FormattingModel model) {
    this.model = model;
  }

  public void run() {
    switch (model) {
    case SDF:
      sdfRun();
      break;
    case JODA:
      jodaRun();
      break;
    }
  }

  void sdfRun() {

    for (;;) {
      synchronized (SDF) {
        long now = System.currentTimeMillis();
        if (CACHE != now) {
          CACHE = now;
          SDF.format(now);
        }
      }
      counter++;
      if (done) {
        return;
      }
    }
  }

  void jodaRun() {
    for (;;) {
      long now = System.currentTimeMillis();
      if (isCacheStale(now)) {
        JODA.print(now);
      }
      counter++;
      if (done) {
        return;
      }
    }
  }
  
  private static boolean isCacheStale(long now) {
    synchronized (JODA) {
      if (CACHE != now) {
        CACHE = now;
        return true;
      }
    }
    return false;
  }

}
