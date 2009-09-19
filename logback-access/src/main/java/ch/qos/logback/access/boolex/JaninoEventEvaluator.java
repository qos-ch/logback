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
package ch.qos.logback.access.boolex;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.boolex.JaninoEventEvaluatorBase;
import ch.qos.logback.core.boolex.Matcher;

public class JaninoEventEvaluator extends JaninoEventEvaluatorBase<AccessEvent> {

  public final static List<String> DEFAULT_PARAM_NAME_LIST = new ArrayList<String>();
  public final static List<Class> DEFAULT_PARAM_TYPE_LIST = new ArrayList<Class>();
  
  static {
    DEFAULT_PARAM_NAME_LIST.add("event");
    DEFAULT_PARAM_TYPE_LIST.add(AccessEvent.class);
  }
  
  
  public JaninoEventEvaluator() {
    
  }
  
  protected String getDecoratedExpression() {
    return getExpression();
  }

  protected String[] getParameterNames() {
    List<String> fullNameList = new ArrayList<String>();
    fullNameList.addAll(DEFAULT_PARAM_NAME_LIST);

    for(int i = 0; i < matcherList.size(); i++) {
      Matcher m = (Matcher) matcherList.get(i);
      fullNameList.add(m.getName());
    }
    
    return (String[]) fullNameList.toArray(CoreConstants.EMPTY_STRING_ARRAY);
  }

  protected Class[] getParameterTypes() {
    List<Class> fullTypeList = new ArrayList<Class>();
    fullTypeList.addAll(DEFAULT_PARAM_TYPE_LIST);
    for(int i = 0; i < matcherList.size(); i++) {
      fullTypeList.add(Matcher.class);
    }
    return (Class[]) fullTypeList.toArray(CoreConstants.EMPTY_CLASS_ARRAY);
  }

  protected Object[] getParameterValues(AccessEvent event) {
    AccessEvent accessEvent = (AccessEvent) event;
    final int matcherListSize = matcherList.size();
    
    int i = 0;
    Object[] values = new Object[DEFAULT_PARAM_NAME_LIST.size()+matcherListSize];

    values[i++] = accessEvent;
    
    for(int j = 0; j < matcherListSize; j++) {
      values[i++] = (Matcher) matcherList.get(j);
    }
    
    return values;
  }

}
