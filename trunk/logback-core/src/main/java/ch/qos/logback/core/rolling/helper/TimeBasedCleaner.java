package ch.qos.logback.core.rolling.helper;

import java.io.File;
import java.util.Date;

public class TimeBasedCleaner {

  FileNamePattern fileNamePattern;
  RollingCalendar rc;
  int numberOfPeriods;

  public TimeBasedCleaner(FileNamePattern fileNamePattern, RollingCalendar rc,
      int numberOfPeriods) {
    this.fileNamePattern = fileNamePattern;
    this.rc = rc;
    //
    this.numberOfPeriods = -numberOfPeriods -1;
  }

  public void clean(Date now) {
    Date date2delete = rc.getRelativeDate(now, numberOfPeriods);

    String filename = fileNamePattern.convertDate(date2delete);

    File file2Delete = new File(filename);

    if (file2Delete.exists() && file2Delete.isFile()) {
      file2Delete.delete();
    }
  }

}
