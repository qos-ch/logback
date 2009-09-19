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
package ch.qos.logback.core.pattern.parser;

import java.util.List;

public class KeywordNode extends FormattingNode {

  List optionList;

  KeywordNode(Object value) {
    super(Node.KEYWORD, value);
  }

  public List getOptions() {
    return optionList;
  }

  public void setOptions(List optionList) {
    this.optionList = optionList;
  }

  public boolean equals(Object o) {
    // System.out.println("Keyword.equals()");
    if (!super.equals(o)) {
      return false;
    }

    if (!(o instanceof KeywordNode)) {
      return false;
    }
    KeywordNode r = (KeywordNode) o;

    return (optionList != null ? optionList.equals(r.optionList)
        : r.optionList == null);
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    if (optionList == null) {
      buf.append("KeyWord(" + value + "," + formatInfo + ")");
    } else {
      buf.append("KeyWord(" + value + ", " + formatInfo + "," + optionList
          + ")");
    }
    buf.append(printNext());
    return buf.toString();
  }
}
