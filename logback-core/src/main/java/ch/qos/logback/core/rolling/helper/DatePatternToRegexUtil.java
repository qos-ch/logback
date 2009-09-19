/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.rolling.helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is concerned with computing a regex corresponding to a date
 * pattern (in {@link SimpleDateFormat} format).
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class DatePatternToRegexUtil {

  final String datePattern;
  final int length;

  DatePatternToRegexUtil(String datePattern) {
    this.datePattern = datePattern;
    length = datePattern.length();
  }

  String toRegex() {
    List<SequenceToRegex4SDF> sequenceList = tokenize();
    StringBuilder sb = new StringBuilder();
    for (SequenceToRegex4SDF seq : sequenceList) {
      sb.append(seq.toRegex());
    }
    return sb.toString();
  }

  private List<SequenceToRegex4SDF> tokenize() {
    List<SequenceToRegex4SDF> sequenceList = new ArrayList<SequenceToRegex4SDF>();
    SequenceToRegex4SDF sequence = null;
    for (int i = 0; i < length; i++) {
      char t = datePattern.charAt(i);
      if (sequence == null || sequence.c != t) {
        sequence = addNewSequence(sequenceList, t);
      } else {
        sequence.inc();
      }
    }
    return sequenceList;
  }

  SequenceToRegex4SDF addNewSequence(List<SequenceToRegex4SDF> sequenceList,
      char t) {
    SequenceToRegex4SDF token = new SequenceToRegex4SDF(t);
    sequenceList.add(token);
    return token;
  }
}
