/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
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
public class DatePatternToSRegexUtil {

  final String datePattern;
  final int length;

  DatePatternToSRegexUtil(String datePattern) {
    this.datePattern = datePattern;
    length = datePattern.length();
  }

  String toSRegex() {
    List<SequenceToRegex4SDF> sequenceList = tokenize();
    StringBuilder sb = new StringBuilder();
    for (SequenceToRegex4SDF seq : sequenceList) {
      sb.append(seq.toSRegex());
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
