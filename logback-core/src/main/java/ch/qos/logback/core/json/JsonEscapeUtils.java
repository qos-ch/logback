package ch.qos.logback.core.json;

/**
 * @author Pierre Queinnec
 */
public class JsonEscapeUtils {

  public static String escape(String stringToEscape) {
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < stringToEscape.length(); i++) {
      char currCharacter = stringToEscape.charAt(i);
      switch (currCharacter) {
      case '\"':
        builder.append("\\\"");
        break;

      case '\\':
        builder.append("\\\\");
        break;

      case '\b':
        builder.append("\\b");
        break;

      case '\f':
        builder.append("\\f");
        break;

      case '\n':
        builder.append("\\n");
        break;

      case '\r':
        builder.append("\\r");
        break;

      case '\t':
        builder.append("\\t");
        break;

      default:
        builder.append(currCharacter);
        break;
      }
    }

    return builder.toString();
  }

}
