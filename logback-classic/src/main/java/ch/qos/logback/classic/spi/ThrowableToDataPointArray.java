/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.spi;

import java.util.LinkedList;
import java.util.List;

import ch.qos.logback.core.CoreConstants;

/**
 * Convert a throwable into an array of ThrowableDataPoint objects.
 * 
 *  
 * @author Ceki G&uuml;lc&uuml;
 */
public class ThrowableToDataPointArray {

  static final ThrowableDataPoint[] TEMPLATE_ARRAY = new ThrowableDataPoint[0];

  
  static public ThrowableDataPoint[] convert(Throwable t) {
    List<ThrowableDataPoint> tdpList = new LinkedList<ThrowableDataPoint>();
    extract(tdpList, t, null);
    return tdpList.toArray(TEMPLATE_ARRAY);
  }

  static private void extract(List<ThrowableDataPoint> tdpList, Throwable t,
      StackTraceElement[] parentSTE) {
    StackTraceElement[] ste = t.getStackTrace();
    final int numberOfcommonFrames = STEUtil.findNumberOfCommonFrames(ste, parentSTE);

    tdpList.add(firstLineToDataPoint(t, parentSTE));
    for (int i = 0; i < (ste.length - numberOfcommonFrames); i++) {
      tdpList.add(new ThrowableDataPoint(ste[i]));
    }
    
    if (numberOfcommonFrames != 0) {
      tdpList.add(new ThrowableDataPoint("\t... "+numberOfcommonFrames
          + " common frames omitted"));
    }

    Throwable cause = t.getCause();
    if (cause != null) {
      extract(tdpList, cause, ste);
    }
  }

  private static ThrowableDataPoint firstLineToDataPoint(Throwable t,
      StackTraceElement[] parentSTE) {
    String prefix = "";
    if (parentSTE != null) {
      prefix = CoreConstants.CAUSED_BY;
    }

    String result = prefix + t.getClass().getName();
    if (t.getMessage() != null) {
      result += ": " + t.getMessage();
    }
    return new ThrowableDataPoint(result);
  }

 

}
