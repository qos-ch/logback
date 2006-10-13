/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreGlobal;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.status.ErrorStatus;


/**
 * This converter outputs caller data depending on depth and marker data.
 * 
 * @author Ceki Gulcu
 */
public class CallerDataConverter extends ClassicConverter {

  int depth = 5;
  List<EventEvaluator> evaluatorList = null;

  final int MAX_ERROR_COUNT = 4;
  int errorCount = 0;
  
  public void start() {
    String depthStr = getFirstOption();
    if (depthStr == null) {
      return;
    }

    try {
      depth = Integer.parseInt(depthStr);
    } catch (NumberFormatException nfe) {
      addError("");
    }

    final List optionList = getOptionList();

    if (optionList != null && optionList.size() > 1) {
      final int optionListSize = optionList.size();
      for (int i = 1; i < optionListSize; i++) {
        String evaluatorStr = (String) optionList.get(i);
        Context context = getContext();
        if (context != null) {
          Map evaluatorMap = (Map) context.getObject(CoreGlobal.EVALUATOR_MAP);
          EventEvaluator ee = (EventEvaluator) evaluatorMap.get(evaluatorStr);
          if (ee != null) {
            addEvaluator(ee);
          }
        }
      }
    }

  }

  private void addEvaluator(EventEvaluator ee) {
    if (evaluatorList == null) {
      evaluatorList = new ArrayList<EventEvaluator>();
    }
    evaluatorList.add(ee);
  }

  public String convert(Object event) {

    LoggingEvent le = (LoggingEvent) event;
    StringBuffer buf = new StringBuffer();

    if (evaluatorList != null) {
      boolean printCallerData = false;
      for (int i = 0; i < evaluatorList.size(); i++) {
        EventEvaluator ee = (EventEvaluator) evaluatorList.get(i);
        try {
          if (ee.evaluate(event)) {
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
            errorStatus.add(new ErrorStatus("This was the last warning about this evaluator's errors." +
                                "We don't want the StatusManager to get flooded.", this));
            addStatus(errorStatus);
          }

        }
      }

      if (!printCallerData) {
        return CoreGlobal.EMPTY_STRING;
      }
    }

    CallerData[] cda = le.getCallerData();
    if (cda != null && cda.length > 0) {
      int limit = depth < cda.length ? depth : cda.length;

      for (int i = 0; i < limit; i++) {
        buf.append("Caller+");
        buf.append(i);
        buf.append("\t at ");
        buf.append(cda[i]);
        buf.append(CoreGlobal.LINE_SEPARATOR);
      }
      return buf.toString();
    } else {
      return CallerData.CALLER_DATA_NA;
    }
  }
}
