package ch.qos.logback.classic.db.nosql;

import ch.qos.logback.classic.db.DBHelper;
import ch.qos.logback.classic.db.names.ColumnName;
import ch.qos.logback.classic.spi.*;
import ch.qos.logback.core.CoreConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation for {@link ch.qos.logback.classic.spi.LoggingEvent} event type.
 *
 * @author Juan Uys
 */
public class RedisAppender extends RedisAppenderBase<LoggingEvent> {

  /**
   * Saves each constituent of {@link ch.qos.logback.classic.spi.LoggingEvent} as a
   * Redis key/value entry.
   *
   * @param event
   * @param eventId
   */
  @Override
  public void subAppend(final LoggingEvent event, final Integer eventId) {

    final String eventIdStr = eventId + ":";

    // for "logging_event" equivalent
    set(eventIdStr + ColumnName.TIMESTMP.toString().toLowerCase(), event.getTimeStamp());
    set(eventIdStr + ColumnName.FORMATTED_MESSAGE.toString().toLowerCase(), event.getFormattedMessage());
    set(eventIdStr + ColumnName.LOGGER_NAME.toString().toLowerCase(), event.getLoggerName());
    set(eventIdStr + ColumnName.LEVEL_STRING.toString().toLowerCase(), event.getLevel().toString());
    set(eventIdStr + ColumnName.THREAD_NAME.toString().toLowerCase(), event.getThreadName());
    set(eventIdStr + ColumnName.REFERENCE_FLAG.toString().toLowerCase(), DBHelper.computeReferenceMask(event));

    event_arguments: {
      int arrayLen = event.getArgumentArray() != null ? event.getArgumentArray().length : 0;

      for(int i = 0; i < arrayLen; i++) {
        set(eventIdStr + "arg" + i, DBHelper.truncateTo254(event.getArgumentArray()[i].toString()));
      }
    }

    caller_data: {
      StackTraceElement callerData = event.getCallerData()[0];
      if (callerData != null) {
        set(eventIdStr + ColumnName.CALLER_FILENAME.toString().toLowerCase(), callerData.getFileName());
        set(eventIdStr + ColumnName.CALLER_CLASS.toString().toLowerCase(), callerData.getClassName());
        set(eventIdStr + ColumnName.CALLER_METHOD.toString().toLowerCase(), callerData.getMethodName());
        set(eventIdStr + ColumnName.CALLER_LINE.toString().toLowerCase(), Integer.toString(callerData.getLineNumber()));
      }
    }

    // for "logging_event_property" equivalent
    final Map<String, String> merged =  DBHelper.mergePropertyMaps(event);
    for (Map.Entry<String, String> entry : merged.entrySet()) {
      set(eventIdStr + "mdc:" + entry.getKey(), entry.getValue());
    }

    // for "logging_event_exception" equivalent
    if (event.getThrowableProxy() != null) {
      short baseIndex = 0;
      IThrowableProxy tp = event.getThrowableProxy();
      while (tp != null) {

        exception_firstline: {
          StringBuilder buf = new StringBuilder();
          ThrowableProxyUtil.printFirstLine(buf, tp);
          set(eventIdStr + "exception:" + baseIndex++, buf.toString());
        }

        // exception common frames
        int commonFrames = tp.getCommonFrames();
        StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
        for (int i = 0; i < stepArray.length - commonFrames; i++) {
          StringBuilder sb = new StringBuilder();
          sb.append(CoreConstants.TAB);
          ThrowableProxyUtil.printSTEP(sb, stepArray[i]);
          set(eventIdStr + "exception:" + baseIndex++, sb.toString());
        }

        // exception ommitted frames
        if (commonFrames > 0) {
          StringBuilder sb = new StringBuilder();
          sb.append(CoreConstants.TAB).append("... ").append(commonFrames).append(
          " common frames omitted");
          set(eventIdStr + "exception:" + baseIndex++, sb.toString());
        }

        tp = tp.getCause();
      }
    }
  }
}
