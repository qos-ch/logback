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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.status.ErrorStatus;

/**
 * This converter outputs caller data depending on depth and marker data.
 * 
 * @author Ceki Gulcu
 */
public class CallerDataConverter extends ClassicConverter {

  public static final String DEFAULT_CALLER_LINE_PREFIX = "Caller+";

  int depth = 5;
  List<EventEvaluator<ILoggingEvent>> evaluatorList = null;


  final int MAX_ERROR_COUNT = 4;
  int errorCount = 0;

  @SuppressWarnings("unchecked")
  public void start() {
    String depthStr = getFirstOption();
    if (depthStr == null) {
      return;
    }

    try {
      depth = Integer.parseInt(depthStr);
    } catch (NumberFormatException nfe) {
      addError("Failed to parse depth option [" + depthStr + "]", nfe);
    }

    final List optionList = getOptionList();

    if (optionList != null && optionList.size() > 1) {
      final int optionListSize = optionList.size();
      for (int i = 1; i < optionListSize; i++) {
        String evaluatorStr = (String) optionList.get(i);
        Context context = getContext();
        if (context != null) {
          Map evaluatorMap = (Map) context
              .getObject(CoreConstants.EVALUATOR_MAP);
          EventEvaluator<ILoggingEvent> ee = (EventEvaluator<ILoggingEvent>) evaluatorMap
              .get(evaluatorStr);
          if (ee != null) {
            addEvaluator(ee);
          }
        }
      }
    }
  }

  private void addEvaluator(EventEvaluator<ILoggingEvent> ee) {
    if (evaluatorList == null) {
      evaluatorList = new ArrayList<EventEvaluator<ILoggingEvent>>();
    }
    evaluatorList.add(ee);
  }

  public String convert(ILoggingEvent le) {
    StringBuilder buf = new StringBuilder();

    if (evaluatorList != null) {
      boolean printCallerData = false;
      for (int i = 0; i < evaluatorList.size(); i++) {
        EventEvaluator<ILoggingEvent> ee = evaluatorList.get(i);
        try {
          if (ee.evaluate(le)) {
            printCallerData = true;
            break;
          }
        } catch (EvaluationException eex) {
          errorCount++;
          if (errorCount < MAX_ERROR_COUNT) {
            addError("Exception thrown for evaluator named [" + ee.getName()
                + "]", eex);
          } else if (errorCount == MAX_ERROR_COUNT) {
            ErrorStatus errorStatus = new ErrorStatus(
                "Exception thrown for evaluator named [" + ee.getName() + "].",
                this, eex);
            errorStatus.add(new ErrorStatus(
                "This was the last warning about this evaluator's errors."
                    + "We don't want the StatusManager to get flooded.", this));
            addStatus(errorStatus);
          }

        }
      }

      if (!printCallerData) {
        return CoreConstants.EMPTY_STRING;
      }
    }

    StackTraceElement[] cda = le.getCallerData();
    if (cda != null && cda.length > 0) {
      int limit = depth < cda.length ? depth : cda.length;

      for (int i = 0; i < limit; i++) {
        buf.append(getCallerLinePrefix());
        buf.append(i);
        buf.append("\t at ");
        buf.append(cda[i]);
        buf.append(CoreConstants.LINE_SEPARATOR);
      }
      return buf.toString();
    } else {
      return CallerData.CALLER_DATA_NA;
    }
  }

  protected String getCallerLinePrefix() {
    return DEFAULT_CALLER_LINE_PREFIX;
  }
}
