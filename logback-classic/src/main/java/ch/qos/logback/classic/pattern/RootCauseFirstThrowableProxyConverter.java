/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;

/**
 * @author Tomasz Nurkiewicz
 * @since 0.9.30
 */
public class RootCauseFirstThrowableProxyConverter extends ExtendedThrowableProxyConverter {

  @Override
  protected String throwableProxyToString(IThrowableProxy tp) {
    StringBuilder buf = new StringBuilder(2048);
    subjoinRootCauseFirst(tp, buf);
    return buf.toString();
  }

  private void subjoinRootCauseFirst(IThrowableProxy tp, StringBuilder buf) {
    if (tp.getCause() != null)
      subjoinRootCauseFirst(tp.getCause(), buf);
    subjoinRootCause(tp, buf);
  }

  private void subjoinRootCause(IThrowableProxy tp, StringBuilder buf) {
    ThrowableProxyUtil.subjoinFirstLineRootCauseFirst(buf, tp);
    buf.append(CoreConstants.LINE_SEPARATOR);
    StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
    int commonFrames = tp.getCommonFrames();

    boolean unrestrictedPrinting = lengthOption > stepArray.length;


    int maxIndex = (unrestrictedPrinting) ? stepArray.length : lengthOption;
    if (commonFrames > 0 && unrestrictedPrinting) {
      maxIndex -= commonFrames;
    }

    for (int i = 0; i < maxIndex; i++) {
      String string = stepArray[i].toString();
      buf.append(CoreConstants.TAB);
      buf.append(string);
      extraData(buf, stepArray[i]); // allow other data to be added
      buf.append(CoreConstants.LINE_SEPARATOR);
    }

  }


}
