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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
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

  protected static final int BUILDER_CAPACITY = 2048;

  int lengthOption;
  List<EventEvaluator<ILoggingEvent>> evaluatorList = null;

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
        lengthOption = 1;
      } else {
        try {
          lengthOption = Integer.parseInt(lengthStr);
        } catch (NumberFormatException nfe) {
          addError("Could not parse [" + lengthStr + "] as an integer");
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
        EventEvaluator<ILoggingEvent> ee = (EventEvaluator<ILoggingEvent>) evaluatorMap
                .get(evaluatorStr);
        addEvaluator(ee);
      }
    }
    super.start();
  }

  private void addEvaluator(EventEvaluator<ILoggingEvent> ee) {
    if (evaluatorList == null) {
      evaluatorList = new ArrayList<EventEvaluator<ILoggingEvent>>();
    }
    evaluatorList.add(ee);
  }

  public void stop() {
    evaluatorList = null;
    super.stop();
  }

  protected void extraData(StringBuilder builder, StackTraceElementProxy step) {
    // nop
  }

  public String convert(ILoggingEvent event) {

    IThrowableProxy tp = event.getThrowableProxy();
    if (tp == null) {
      return CoreConstants.EMPTY_STRING;
    }

    // an evaluator match will cause stack printing to be skipped
    if (evaluatorList != null) {
      boolean printStack = true;
      for (int i = 0; i < evaluatorList.size(); i++) {
        EventEvaluator<ILoggingEvent> ee = evaluatorList.get(i);
        try {
          if (ee.evaluate(event)) {
            printStack = false;
            break;
          }
        } catch (EvaluationException eex) {
          errorCount++;
          if (errorCount < CoreConstants.MAX_ERROR_COUNT) {
            addError("Exception thrown for evaluator named [" + ee.getName()
                    + "]", eex);
          } else if (errorCount == CoreConstants.MAX_ERROR_COUNT) {
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

      if (!printStack) {
        return CoreConstants.EMPTY_STRING;
      }
    }

    return throwableProxyToString(tp);
  }

  protected String throwableProxyToString(IThrowableProxy tp) {
    StringBuilder sb = new StringBuilder(BUILDER_CAPACITY);

    recursiveAppend(sb, null, ThrowableProxyUtil.REGULAR_EXCEPTION_INDENT, tp);

    return sb.toString();
  }

  private void recursiveAppend(StringBuilder sb, String prefix, int indent, IThrowableProxy tp) {
    if(tp == null)
      return;
    subjoinFirstLine(sb, prefix, indent, tp);
    sb.append(CoreConstants.LINE_SEPARATOR);
    subjoinSTEPArray(sb, indent, tp);
    IThrowableProxy[] suppressed = tp.getSuppressed();
    if(suppressed != null) {
      for(IThrowableProxy current : suppressed) {
        recursiveAppend(sb, CoreConstants.SUPPRESSED, indent + ThrowableProxyUtil.SUPPRESSED_EXCEPTION_INDENT, current);
      }
    }
    recursiveAppend(sb, CoreConstants.CAUSED_BY, indent, tp.getCause());
  }

  private void subjoinFirstLine(StringBuilder buf, String prefix, int indent, IThrowableProxy tp) {
    ThrowableProxyUtil.indent(buf, indent - 1);
    if (prefix != null) {
      buf.append(prefix);
    }
    subjoinExceptionMessage(buf, tp);
  }

  private void subjoinExceptionMessage(StringBuilder buf, IThrowableProxy tp) {
    buf.append(tp.getClassName()).append(": ").append(tp.getMessage());
  }

  protected void subjoinSTEPArray(StringBuilder buf, int indent, IThrowableProxy tp) {
    StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
    int commonFrames = tp.getCommonFrames();

    boolean unrestrictedPrinting = lengthOption > stepArray.length;


    int maxIndex = (unrestrictedPrinting) ? stepArray.length : lengthOption;
    if (commonFrames > 0 && unrestrictedPrinting) {
      maxIndex -= commonFrames;
    }

    for (int i = 0; i < maxIndex; i++) {
      ThrowableProxyUtil.indent(buf, indent);
      buf.append(stepArray[i]);
      extraData(buf, stepArray[i]); // allow other data to be added
      buf.append(CoreConstants.LINE_SEPARATOR);
    }

    if (commonFrames > 0 && unrestrictedPrinting) {
      ThrowableProxyUtil.indent(buf, indent);
      buf.append("... ").append(tp.getCommonFrames()).append(
              " common frames omitted").append(CoreConstants.LINE_SEPARATOR);
    }
  }
}
