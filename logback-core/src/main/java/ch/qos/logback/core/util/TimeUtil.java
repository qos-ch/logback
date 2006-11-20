package ch.qos.logback.core.util;

import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

  static long computeStartOfNextDay(long now) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date(now));

    cal.add(Calendar.DAY_OF_MONTH, 1);
    cal.set(Calendar.MILLISECOND, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    return cal.getTime().getTime();
  }
  
//  static long computeStartOfNextWeek(long now) {
//    Calendar cal = Calendar.getInstance();
//    cal.setTime(new Date(now));
//
//    cal.add(Calendar.DAY_OF_MONTH, getFirstDayOfWeek());
//    
//    
//    cal.set(Calendar.HOUR_OF_DAY, 0);
//    cal.set(Calendar.MINUTE, 0);
//    cal.set(Calendar.SECOND, 0);
//    cal.set(Calendar.MILLISECOND, 0);
//    
//    return cal.getTime().getTime();
//  }
  
//  this.set(Calendar.DAY_OF_WEEK, getFirstDayOfWeek());
//  this.set(Calendar.HOUR_OF_DAY, 0);
//  this.set(Calendar.MINUTE, 0);
//  this.set(Calendar.SECOND, 0);
//  this.set(Calendar.MILLISECOND, 0);
//  this.add(Calendar.WEEK_OF_YEAR, 1);

  
}
