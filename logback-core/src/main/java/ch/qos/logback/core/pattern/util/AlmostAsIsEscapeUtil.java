package ch.qos.logback.core.pattern.util;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.rolling.helper.FileNamePattern;

/**
 * This implementation is intended for use in {@link FileNamePattern}.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class AlmostAsIsEscapeUtil implements IEscapeUtil {

  /**
   * Do not perform any character escaping, except for '%'.
   * 
   * <p>
   * Here is the rationale. First, filename patterns do not include escape
   * combinations such as \r or \n. Moreover, characters which have special
   * meaning in logback parsers, such as '(', ')', '{', or '}' cannot be part of
   * file names (so me thinks). Thus, the only character that needs escaping is
   * '%'.
   * 
   * <p>
   * Note that this method assumes that it is called after the escape character
   * has been consumed.
   */
  public void escape(String escapeChars, StringBuffer buf, char next,
      int pointer) {

    if (next == CoreConstants.PERCENT_CHAR) {
      buf.append(CoreConstants.PERCENT_CHAR);
    } else {
      // restitute the escape char (because it was consumed 
      // before this method was called).
      buf.append("\\");
      // restitute the next character
      buf.append(next);
    }
  }
}
