package ch.qos.logback.classic.json;

import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.json.JsonLayoutBase;

/**
 * A brain-dead implementation with no external dependencies
 * of a layout producing a simple JSON like this:
 * <code>
 * {
 *  "level": "INFO",
 *  "logger": "ch.qos.logback.classic.pattern.ConverterTest",
 *  "formatted-message": "Some message",
 *  "mdc": {
 *     "suspicious": "true",
 *     "user": "joe"
 *  },
 *  "timestamp": 1310823045238
 * }
 * </code>
 * @author Pierre Queinnec
 */
public class JsonLayout extends JsonLayoutBase<ILoggingEvent> {

  protected boolean includeLevel;
  protected boolean includeLoggerName;
  protected boolean includeFormattedMessage;
  protected boolean includeMessage;
  protected boolean includeMDC;
  protected boolean includeThreadName;

  public JsonLayout() {
    super();

    // defaults
    this.includeLevel = true;
    this.includeLoggerName = true;
    this.includeFormattedMessage = true;
    this.includeMDC = true;
  }

  public String doLayout(ILoggingEvent event) {
    boolean hasAlreadyOneElement = false;
    StringBuilder builder = new StringBuilder();
    builder.append('{');

    if (this.includeLevel && (event.getLevel() != null)) {
      hasAlreadyOneElement = true;

      builder.append("\"level\":\"");
      builder.append(event.getLevel());
      builder.append('\"');
    }

    if (this.includeLoggerName && (event.getLoggerName() != null)) {
      if (hasAlreadyOneElement) {
        builder.append(',');
      } else {
        hasAlreadyOneElement = true;
      }

      builder.append("\"logger\":\"");
      builder.append(event.getLoggerName());
      builder.append('\"');
    }

    if (this.includeFormattedMessage && (event.getFormattedMessage() != null)) {
      if (hasAlreadyOneElement) {
        builder.append(',');
      } else {
        hasAlreadyOneElement = true;
      }

      builder.append("\"formatted-message\":\"");
      builder.append(event.getFormattedMessage());
      builder.append('\"');
    }

    if (this.includeMDC) {
      Map<String, String> mdc = event.getMDCPropertyMap();
      if ((mdc != null) && !mdc.isEmpty()) {
        if (hasAlreadyOneElement) {
          builder.append(',');
        } else {
          hasAlreadyOneElement = true;
        }

        boolean hasAlreadyOneMdcElement = false;
        builder.append("\"mdc\":{");

        for (Map.Entry<String, String> currEntry : mdc.entrySet()) {
          if (hasAlreadyOneMdcElement) {
            builder.append(',');
          } else {
            hasAlreadyOneMdcElement = true;
          }

          builder.append('\"');
          builder.append(currEntry.getKey());
          builder.append("\":\"");
          builder.append(currEntry.getValue());
          builder.append('\"');
        }

        builder.append('}');
      }
    }

    if (this.includeMessage && (event.getMessage() != null)) {
      if (hasAlreadyOneElement) {
        builder.append(',');
      } else {
        hasAlreadyOneElement = true;
      }

      builder.append("\"message\":\"");
      builder.append(event.getMessage());
      builder.append('\"');
    }

    if (this.includeThreadName && (event.getThreadName() != null)) {
      if (hasAlreadyOneElement) {
        builder.append(',');
      } else {
        hasAlreadyOneElement = true;
      }

      builder.append("\"thread\":\"");
      builder.append(event.getThreadName());
      builder.append('\"');
    }

    if (this.includeTimestamp) {
      if (hasAlreadyOneElement) {
        builder.append(',');
      } else {
        hasAlreadyOneElement = true;
      }

      builder.append("\"timestamp\":");
      builder.append(event.getTimeStamp());
    }

    builder.append('}');

    return builder.toString();
  }

  public void setIncludeLevel(boolean includeLevel) {
    this.includeLevel = includeLevel;
  }

  public void setIncludeLoggerName(boolean includeLoggerName) {
    this.includeLoggerName = includeLoggerName;
  }

  public void setIncludeFormattedMessage(boolean includeFormattedMessage) {
    this.includeFormattedMessage = includeFormattedMessage;
  }

  public void setIncludeMDC(boolean includeMDC) {
    this.includeMDC = includeMDC;
  }

  public void setIncludeMessage(boolean includeMessage) {
    this.includeMessage = includeMessage;
  }

  public void setIncludeThreadName(boolean includeThreadName) {
    this.includeThreadName = includeThreadName;
  }

}
