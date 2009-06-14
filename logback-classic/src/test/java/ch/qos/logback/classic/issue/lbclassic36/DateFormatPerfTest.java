package ch.qos.logback.classic.issue.lbclassic36;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateFormatPerfTest {
  public static final String ISO8601_PATTERN = "yyyy-MM-dd HH:mm:ss,SSS";
  static final long NANOS_IN_ONE_SEC = 1000 * 1000 * 1000L;

  static long RUN_LENGTH = 1000 * 1000;

  public static void main(String[] args) {
    for (int i = 0; i < 5; i++) {
      doRawJoda();
      doRawSDF();
      doGuardedJoda();
      doGuardedSDF();
    }

    print("Raw Joda:     ", doRawJoda());
    print("Raw SDF:      ", doRawSDF());
    print("Guarded Joda: ", doGuardedJoda());
    print("Guarded SDF:  ", doGuardedSDF());

  }

  static void print(String msg, double avg) {
    System.out.println(msg + " average tick " + avg + " nanoseconds");
  }

  static double doRawJoda() {
    DateTimeFormatter jodaFormat = DateTimeFormat.forPattern(ISO8601_PATTERN);
    long timeInMillis = new Date().getTime();
    long start = System.nanoTime();
    for (int i = 0; i < RUN_LENGTH; ++i) {
      jodaFormat.print(timeInMillis);
    }
    return (System.nanoTime() - start) * 1.0d / RUN_LENGTH;
  }

  static double doGuardedJoda() {
    DateTimeFormatter jodaFormat = DateTimeFormat.forPattern(ISO8601_PATTERN);
    long start = System.nanoTime();
    long cache = 0;
    for (int i = 0; i < RUN_LENGTH; ++i) {
      synchronized (jodaFormat) {
        long now = System.currentTimeMillis();
        if (cache != now) {
          cache = now;
          jodaFormat.print(now);
        }
      }
    }
    return (System.nanoTime() - start) * 1.0d / RUN_LENGTH;
  }

  static double doRawSDF() {
    SimpleDateFormat simpleFormat = new SimpleDateFormat(ISO8601_PATTERN);
    long timeInMillis = new Date().getTime();
    long start = System.nanoTime();
    for (int i = 0; i < RUN_LENGTH; ++i) {
      simpleFormat.format(timeInMillis);
    }
    return (System.nanoTime() - start) * 1.0d / RUN_LENGTH;
  }

  static double doGuardedSDF() {
    SimpleDateFormat simpleFormat = new SimpleDateFormat(ISO8601_PATTERN);
    long start = System.nanoTime();
    long cache = 0;
    for (int i = 0; i < RUN_LENGTH; ++i) {
      synchronized (simpleFormat) {
        long now = System.currentTimeMillis();
        if (cache != now) {
          cache = now;
          simpleFormat.format(now);
        }
      }
    }
    return (System.nanoTime() - start) * 1.0d / RUN_LENGTH;
  }
}
