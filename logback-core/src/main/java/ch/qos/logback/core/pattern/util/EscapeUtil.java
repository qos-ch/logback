
package ch.qos.logback.core.pattern.util;

public class EscapeUtil {
	
	public static void escape(String escapeChars, StringBuffer buf, char next, int pointer) {
		if (escapeChars.indexOf(next) >= 0) {
			buf.append(next);
		} else
			switch (next) {
      case '_':
        // the \_ sequence is swallowed
        break;
			case '\\':
				buf.append(next);
				break;
			case 't':
				buf.append('\t');
				break;
			case 'r':
				buf.append('\r');
				break;
			case 'n':
				buf.append('\n');
				break;
			default:
				String echars = "";
				for (int i = 0; i < escapeChars.length(); i++) {
					echars += ", \\" + escapeChars.charAt(i);
				}
				new IllegalArgumentException("Illegal char '" + next
						+ " at column " + pointer + ". Only \\\\, \\_" + echars
						+ ", \\t, \\n, \\r combinations are allowed as escape characters.");
			}
	}
  
  public static String basicEscape(String s) {
    char c;
    int len = s.length();
    StringBuffer sbuf = new StringBuffer(len);

    int i = 0;
    while (i < len) {
      c = s.charAt(i++);
      if (c == '\\') {
        c = s.charAt(i++);
        if (c == 'n') {
          c = '\n';
        } else if (c == 'r') {
          c = '\r';
        } else if (c == 't') {
          c = '\t';
        } else if (c == 'f') {
          c = '\f';
        } else if (c == '\b') {
          c = '\b';
        } else if (c == '\"') {
          c = '\"';
        } else if (c == '\'') {
          c = '\'';
        } else if (c == '\\') {
          c = '\\';
        }
      }
      sbuf.append(c);
    }
    return sbuf.toString();
  }
}

