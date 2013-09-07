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
package ch.qos.logback.core.helpers;

/**
 * Utility class for transforming strings.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Michael A. McAngus
 */
public class Transform {
  private static final String CDATA_START = "<![CDATA[";
  private static final String CDATA_END = "]]>";
  private static final String CDATA_PSEUDO_END = "]]&gt;";
  private static final String CDATA_EMBEDED_END = CDATA_END + CDATA_PSEUDO_END
      + CDATA_START;
  private static final int CDATA_END_LEN = CDATA_END.length();

  /**
   * This method takes a string which may contain HTML tags (ie, &lt;b&gt;,
   * &lt;table&gt;, etc) and replaces any '<' and '>' characters with
   * respective predefined entity references.
   * 
   * @param input
   *          The text to be converted.
   */
  public static String escapeTags(final String input) {
    // if input is null or zero length or contains no < and > characters, return it as is
    if ((input == null) || (input.length() == 0)
        || (input.indexOf("<") == -1 && input.indexOf(">") == -1)) {
      return input;
    }

    StringBuffer buf = new StringBuffer(input);
    return escapeTags(buf);
  }
  

  /**
   * This method takes a StringBuilder which may contain HTML tags (ie, &lt;b&gt;,
   * &lt;table&gt;, etc) and replaces any '<' and '>' characters with
   * respective predefined entity references.
   * @param buf StringBuffer to transform
   * @return
   */
  public static String escapeTags(final StringBuffer buf) {
    for (int i = 0; i < buf.length(); i++) {
      char ch = buf.charAt(i);
      if (ch == '<') {
        buf.replace(i, i + 1, "&lt;");
      } else if (ch == '>') {
        buf.replace(i, i + 1, "&gt;");
      }
    }
    return buf.toString();
  }
  

  /**
   * Ensures that embedded CDEnd strings (]]>) are handled properly within
   * message, NDC and throwable tag text.
   * 
   * @param output
   *          Writer. The initial CDSutart (<![CDATA[) and final CDEnd (]]>) of
   *          the CDATA section are the responsibility of the calling method.
   * 
   * @param str
   *          The String that is inserted into an existing CDATA Section.
   */
  public static void appendEscapingCDATA(StringBuilder output, String str) {
    if (str == null) {
      return;
    }

    int end = str.indexOf(CDATA_END);

    if (end < 0) {
      output.append(str);

      return;
    }

    int start = 0;

    while (end > -1) {
      output.append(str.substring(start, end));
      output.append(CDATA_EMBEDED_END);
      start = end + CDATA_END_LEN;

      if (start < str.length()) {
        end = str.indexOf(CDATA_END, start);
      } else {
        return;
      }
    }

    output.append(str.substring(start));
  }
}
