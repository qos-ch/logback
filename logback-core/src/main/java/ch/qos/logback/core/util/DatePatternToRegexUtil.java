/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is concerned with computing a regex corresponding to a date
 * pattern (in {@link SimpleDateFormat} format).
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class DatePatternToRegexUtil {

    final String datePattern;
    final int datePatternLength;
    final CharSequenceToRegexMapper regexMapper = new CharSequenceToRegexMapper();

    public DatePatternToRegexUtil(String datePattern) {
        this.datePattern = datePattern;
        datePatternLength = datePattern.length();
    }

    public String toRegex() {
        List<CharSequenceState> charSequenceList = tokenize();
        StringBuilder sb = new StringBuilder();
        for (CharSequenceState seq : charSequenceList) {
            sb.append(regexMapper.toRegex(seq));
        }
        return sb.toString();
    }

    private List<CharSequenceState> tokenize() {
        List<CharSequenceState> sequenceList = new ArrayList<CharSequenceState>();

        CharSequenceState lastCharSequenceState = null;

        for (int i = 0; i < datePatternLength; i++) {
            char t = datePattern.charAt(i);
            if (lastCharSequenceState == null || lastCharSequenceState.c != t) {
                lastCharSequenceState = new CharSequenceState(t);
                sequenceList.add(lastCharSequenceState);
            } else {
                lastCharSequenceState.incrementOccurrences();
            }
        }
        return sequenceList;
    }
}
