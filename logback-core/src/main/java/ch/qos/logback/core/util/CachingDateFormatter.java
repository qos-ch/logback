package ch.qos.logback.core.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A synchronized implementation of  SimpleDateFormat which uses caching internally.
 *
 * @author Ceki G&uuml;c&uuml;
 * @since 0.9.29
 */
public class CachingDateFormatter {


  long lastTimestamp = -1;
  String cachedStr = null;
  final SimpleDateFormat sdf;

  public CachingDateFormatter(String pattern) {
    sdf = new SimpleDateFormat(pattern);
  }

  public final String format(long now) {

    // SimpleDateFormat is not thread safe.

    // See also the discussion in http://jira.qos.ch/browse/LBCLASSIC-36
    // DateFormattingThreadedThroughputCalculator and SelectiveDateFormattingRunnable
    // are also note worthy

    // The now == lastTimestamp guard minimizes synchronization
    synchronized (this) {
      if (now != lastTimestamp) {
        lastTimestamp = now;
        cachedStr = sdf.format(new Date(now));
      }
      return cachedStr;
    }
  }

  public void setTimeZone(TimeZone tz) {
    sdf.setTimeZone(tz);
  }
}
