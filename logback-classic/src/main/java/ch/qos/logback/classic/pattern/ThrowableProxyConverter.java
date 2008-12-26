/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableDataPoint;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.status.ErrorStatus;

/**
 * Add a stack trace in case the event contains a Throwable.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class ThrowableProxyConverter extends ThrowableHandlingConverter {

  int lengthOption;
  List<EventEvaluator<LoggingEvent>> evaluatorList = null;

  final int MAX_ERROR_COUNT = 4;
  int errorCount = 0;

  @SuppressWarnings("unchecked")
  public void start() {

    String lengthStr = getFirstOption();

    if (lengthStr == null) {
      lengthOption = Integer.MAX_VALUE;
    } else {
      lengthStr = lengthStr.toLowerCase();
      if ("full".equals(lengthStr)) {
        lengthOption = Integer.MAX_VALUE;
      } else if ("short".equals(lengthStr)) {
        lengthOption = 2;
      } else {
        try {
          // we add one because, printing starts at offset 1
          lengthOption = Integer.parseInt(lengthStr) + 1;
        } catch (NumberFormatException nfe) {
          addError("Could not parser [" + lengthStr + " as an integer");
          lengthOption = Integer.MAX_VALUE;
        }
      }
    }

    final List optionList = getOptionList();

    if (optionList != null && optionList.size() > 1) {
      final int optionListSize = optionList.size();
      for (int i = 1; i < optionListSize; i++) {
        String evaluatorStr = (String) optionList.get(i);
        Context context = getContext();
        Map evaluatorMap = (Map) context.getObject(CoreConstants.EVALUATOR_MAP);
        EventEvaluator<LoggingEvent> ee = (EventEvaluator<LoggingEvent>) evaluatorMap.get(evaluatorStr);
        addEvaluator(ee);
      }
    }
    super.start();
  }

  private void addEvaluator(EventEvaluator<LoggingEvent> ee) {
    if (evaluatorList == null) {
      evaluatorList = new ArrayList<EventEvaluator<LoggingEvent>>();
    }
    evaluatorList.add(ee);
  }

  public void stop() {
    evaluatorList = null;
    super.stop();
  }

  protected void extraData(StringBuilder builder, ThrowableDataPoint tdp) {
    // nop
  }
  
  protected void prepareLoggingEvent(LoggingEvent event) {
    // nop  
  }
  
  public String convert(LoggingEvent event) {
    StringBuilder buf = new StringBuilder(32);

    ThrowableProxy information = event.getThrowableProxy();

    if (information == null) {
      return CoreConstants.EMPTY_STRING;
    }

    ThrowableDataPoint[] tdpArray = information.getThrowableDataPointArray();

    int length = (lengthOption > tdpArray.length) ? tdpArray.length
        : lengthOption;

    // an evaluator match will cause stack printing to be skipped 
    if (evaluatorList != null) {
      boolean printStack = true;
      for (int i = 0; i < evaluatorList.size(); i++) {
        EventEvaluator<LoggingEvent> ee = evaluatorList.get(i);
        try {
          if (ee.evaluate(event)) {
            printStack = false;
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

      if (!printStack) {
        return CoreConstants.EMPTY_STRING;
      }
    }

    prepareLoggingEvent(event);
    
    buf.append(tdpArray[0]).append(CoreConstants.LINE_SEPARATOR);
    for (int i = 1; i < length; i++) {
      String string = tdpArray[i].toString();
      buf.append(string);
      extraData(buf, tdpArray[i]); // allow other data to be appended
      buf.append(CoreConstants.LINE_SEPARATOR);
    }

    return buf.toString();
  }

}
