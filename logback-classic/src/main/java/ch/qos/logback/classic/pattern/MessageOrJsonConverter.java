package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class MessageOrJsonConverter extends ClassicConverter {
  public static final String JSON_PREFIX = "{\"";
  public static final String JSON_SUFFIX = "}";
  public static final String MESSAGE_FIELD_START = "\"message\":\"";
  public static final String MESSAGE_FIELD_END = "\"";

  @Override
  public String convert(ILoggingEvent event) {
    String msg = event.getFormattedMessage();
    if (msg.startsWith(JSON_PREFIX) && msg.endsWith(JSON_SUFFIX)) {
      // msg is already json (we assume), just use the fields
      return msg.substring(1, msg.length() - 1);
    }
    // make the message a json "message" field
    StringBuilder sb = new StringBuilder(msg.length() + 16); // pad to prevent realloc
    sb.append(MESSAGE_FIELD_START);
    jsonEscape(sb, msg);
    sb.append(MESSAGE_FIELD_END);
    return sb.toString();
  }
  
  public static StringBuilder jsonEscape(StringBuilder sb, String s) {
    int len = s.length();
    for (int i = 0; i < len; ++i) {
       char c = s.charAt(i);
       if (c == '\n') {
         sb.append("\\n");
       } else {
         if (c == '\\' || c == '"') {
           sb.append('\\');
         }
         sb.append(c);
       }
    }
    return sb;
  }

}
