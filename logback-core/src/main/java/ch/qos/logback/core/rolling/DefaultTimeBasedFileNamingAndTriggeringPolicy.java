package ch.qos.logback.core.rolling;

import java.io.File;
import java.util.Date;

public class DefaultTimeBasedFileNamingAndTriggeringPolicy<E> extends TimeBasedFileNamingAndTriggeringPolicyBase<E> {



  @Override
  public void start() {
    super.start();
    
    started = true;
  }
  

  public boolean isTriggeringEvent(File activeFile, final E event) {
    long time = getCurrentTime();

    if (time >= nextCheck) {
      Date dateInElapsedPeriod = dateInCurrentPeriod;
      elapsedPeriodsFileName = tbrp.fileNamePatternWCS
          .convert(dateInElapsedPeriod);
      updateDateInCurrentPeriod(time);
      computeNextCheck();
      return true;
    } else {
      return false;
    }
  }
}
