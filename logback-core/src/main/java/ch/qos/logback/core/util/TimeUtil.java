package ch.qos.logback.core.util;

import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

  
  public static long computeStartOfNextSecond(long now) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date(now));
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.SECOND, 1);
    return cal.getTime().getTime();
  }
  
  public static long computeStartOfNextDay(long now) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date(now));

    cal.add(Calendar.DAY_OF_MONTH, 1);
    cal.set(Calendar.MILLISECOND, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    return cal.getTime().getTime();
  }
  
  public static long computeStartOfNextWeek(long now) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date(now));
    
    cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.WEEK_OF_YEAR, 1);
    return cal.getTime().getTime();
  }

  public static long computeStartOfNextMonth(long now) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date(now));

    cal.set(Calendar.DATE, 1);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.MONTH, 1);
    return cal.getTime().getTime();
  }
  
  

}
