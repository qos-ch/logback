package ch.qos.logback.classic.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableInformation;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreGlobal;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluator;


/**
 * Add a stack trace i case the event contains a Throwable.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class ThrowableInformationConverter extends ThrowableHandlingConverter {

  int lengthOption;
  List<EventEvaluator> evaluatorList = null;

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
          addError("Could not parser ["+lengthStr+" as an integer");
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
        Map evaluatorMap = (Map) context.getObject(CoreGlobal.EVALUATOR_MAP);
        EventEvaluator ee = (EventEvaluator) evaluatorMap.get(evaluatorStr);
        addEvaluator(ee);
      }
    }
    super.start();
  }

  private void addEvaluator(EventEvaluator ee) {
    if (evaluatorList == null) {
      evaluatorList = new ArrayList<EventEvaluator>();
    }
    evaluatorList.add(ee);
  }

  public void stop() {
    evaluatorList = null;
    super.stop();
  }

  public String convert(Object event) {
    StringBuffer buf = new StringBuffer(32);

    LoggingEvent le = (LoggingEvent) event;
    ThrowableInformation information = le.getThrowableInformation();

    if (information == null) {
      return CoreGlobal.EMPTY_STRING;
    }

    String[] stringRep = information.getThrowableStrRep();

    int length =  (lengthOption > stringRep.length) ? stringRep.length : lengthOption;

    if (evaluatorList != null) {
      boolean printStack = true;
      for (int i = 0; i < evaluatorList.size(); i++) {
        EventEvaluator ee = (EventEvaluator) evaluatorList.get(i);
        try {
          if (ee.evaluate(event)) {
            printStack = false;
            break;
          }
        } catch (EvaluationException eex) {
          addError("Exception thrown for evaluator named ["+ee.getName()+"]", eex);
        }
      }

      if (!printStack) {
        return CoreGlobal.EMPTY_STRING;
      }
    }

    buf.append(stringRep[0]).append(CoreGlobal.LINE_SEPARATOR);
    for (int i = 1; i < length; i++) {
      String string = stringRep[i];

      if (string.startsWith(ch.qos.logback.classic.ClassicGlobal.CAUSED_BY)) {
        // nothing
      } else if (Character.isDigit(string.charAt(0))) {
        buf.append("\t... ");
      } else {
        buf.append("\tat ");
      }
      buf.append(string).append(CoreGlobal.LINE_SEPARATOR);
    }

    return buf.toString();
  }

}
