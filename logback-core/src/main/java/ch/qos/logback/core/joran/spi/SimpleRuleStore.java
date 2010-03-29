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
package ch.qos.logback.core.joran.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.OptionHelper;

/**
 * This class implements the {@link RuleStore} interface. It is the rule store
 * implementation used by default in Joran.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class SimpleRuleStore extends ContextAwareBase implements RuleStore {

  static String ANY = "*";
  
  // key: Pattern instance, value: ArrayList containing actions
  HashMap<Pattern, List<Action>> rules = new HashMap<Pattern, List<Action>>();

  // public SimpleRuleStore() {
  // }

  public SimpleRuleStore(Context context) {
    setContext(context);
  }

  /**
   * Add a new rule, i.e. a pattern, action pair to the rule store. <p> Note
   * that the added action's LoggerRepository will be set in the process.
   */
  public void addRule(Pattern pattern, Action action) {
    action.setContext(context);

    List<Action> a4p = rules.get(pattern);

    if (a4p == null) {
      a4p = new ArrayList<Action>();
      rules.put(pattern, a4p);
    }

    a4p.add(action);
  }

  public void addRule(Pattern pattern, String actionClassName) {
    Action action = null;

    try {
      action = (Action) OptionHelper.instantiateByClassName(actionClassName,
          Action.class, context);
    } catch (Exception e) {
      addError("Could not instantiate class [" + actionClassName + "]", e);
    }
    if (action != null) {
      addRule(pattern, action);
    }
  }

  // exact match has highest priority
  // if no exact match, check for tail match, i.e matches of type */x/y
  // tail match for */x/y has higher priority than match for */x
  // if no tail match, check for prefix match, i.e. matches for x/*
  // match for x/y/* has higher priority than matches for x/*

  public List matchActions(Pattern currentPattern) {
    List actionList;

    if ((actionList = rules.get(currentPattern)) != null) {
      return actionList;
    } else if ((actionList = tailMatch(currentPattern)) != null) {
      return actionList;
    } else if ((actionList = prefixMatch(currentPattern)) != null) {
      return actionList;
    } else if ((actionList = middleMatch(currentPattern)) != null) { 
      return actionList;
    } else {
      return null;
    }
  }

  List tailMatch(Pattern currentPattern) {
    int max = 0;
    Pattern longestMatchingPattern = null;

    for (Pattern p : rules.keySet()) {

      if ((p.size() > 1) && p.get(0).equals(ANY)) {
        int r = currentPattern.getTailMatchLength(p);
        if (r > max) {
          max = r;
          longestMatchingPattern = p;
        }
      }
    }

    if (longestMatchingPattern != null) {
      return rules.get(longestMatchingPattern);
    } else {
      return null;
    }
  }

  List prefixMatch(Pattern currentPattern) {
    int max = 0;
    Pattern longestMatchingPattern = null;

    for (Pattern p : rules.keySet()) {
      String last = p.peekLast();
      if (ANY.equals(last)) {
        int r = currentPattern.getPrefixMatchLength(p);
        // to qualify the match length must equal p's size omitting the '*'
        if ((r == p.size() - 1) && (r > max)) {
          // System.out.println("New longest prefixMatch "+p);
          max = r;
          longestMatchingPattern = p;
        }
      }
    }

    if (longestMatchingPattern != null) {
      return rules.get(longestMatchingPattern);
    } else {
      return null;
    }
  }

  List middleMatch(Pattern currentPattern) {
    
    int max = 0;
    Pattern longestMatchingPattern = null;

    for (Pattern p : rules.keySet()) {
      String last = p.peekLast();
      String first = null;
      if(p.size() > 1) {
        first = p.get(0);
      }
      if (ANY.equals(last) && ANY.equals(first)) {
        List<String> partList = p.getCopyOfPartList();
        if(partList.size() > 2) {
          partList.remove(0);
          partList.remove(partList.size()-1);
        }
        
        int r = 0;
        Pattern clone = new Pattern(partList);        
        if(currentPattern.isContained(clone)) {
          r = clone.size();
        }
        if (r > max) {
          max = r;
          longestMatchingPattern = p;
        }
      }
    }

    if (longestMatchingPattern != null) {
      return rules.get(longestMatchingPattern);
    } else {
      return null;
    }
  }
  

  public String toString() {
    final String TAB = "  ";

    StringBuilder retValue = new StringBuilder();

    retValue.append("SimpleRuleStore ( ").append("rules = ").append(this.rules)
        .append(TAB).append(" )");

    return retValue.toString();
  }

}
