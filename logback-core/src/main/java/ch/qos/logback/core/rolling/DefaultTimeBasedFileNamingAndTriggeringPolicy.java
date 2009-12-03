/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.rolling;

import java.io.File;
import java.util.Date;

import ch.qos.logback.core.rolling.helper.DefaultArchiveRemover;

/**
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 * @param <E>
 */
public class DefaultTimeBasedFileNamingAndTriggeringPolicy<E> extends TimeBasedFileNamingAndTriggeringPolicyBase<E> {

  @Override
  public void start() {
    super.start();
    archiveRemover = new DefaultArchiveRemover(tbrp.fileNamePattern, rc);
    archiveRemover.setContext(context);
    started = true;
  }
  
  public boolean isTriggeringEvent(File activeFile, final E event) {
    long time = getCurrentTime();
    if (time >= nextCheck) {
      Date dateOfElapsedPeriod = dateInCurrentPeriod;
      elapsedPeriodsFileName = tbrp.fileNamePatternWCS
          .convert(dateOfElapsedPeriod);
      setDateInCurrentPeriod(time);
      computeNextCheck();
      return true;
    } else {
      return false;
    }
  }
}
