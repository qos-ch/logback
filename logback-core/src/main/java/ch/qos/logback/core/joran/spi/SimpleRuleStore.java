/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.joran.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.OptionHelper;



public class SimpleRuleStore extends ContextAwareBase implements RuleStore {

  // key: Pattern instance, value: ArrayList containing actions
  HashMap<Pattern, List<Action>> rules = new HashMap<Pattern, List<Action>>();
  
//  public SimpleRuleStore() {
//  }

  public SimpleRuleStore(Context context) {
    setContext(context);
  }
  
  /**
   * Add a new rule, i.e. a pattern, action pair to the rule store.
   * <p>
   * Note that the added action's LoggerRepository will be set in the
   * process.
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
      action = (Action) OptionHelper.instantiateByClassName(
        actionClassName, Action.class);
    } catch(Exception e) {
      addError("Could not instantiate class ["+actionClassName+"]", e);
    }
    if(action != null) {
        addRule(pattern, action);
      }
  }

  public List matchActions(Pattern pattern) {
    //System.out.println("pattern to search for:" + pattern + ", hashcode: " + pattern.hashCode());
    //System.out.println("rules:" + rules);
    ArrayList a4p = (ArrayList) rules.get(pattern);

    if (a4p != null) {
      return a4p;
    } else {
      Iterator patternsIterator = rules.keySet().iterator();
      int max = 0;
      Pattern longestMatch = null;

      while (patternsIterator.hasNext()) {
        Pattern p = (Pattern) patternsIterator.next();

        if ((p.size() > 1) && p.get(0).equals("*")) {
          int r = pattern.tailMatch(p);

          //System.out.println("tailMatch " +r);
          if (r > max) {
            //System.out.println("New longest match "+p);
            max = r;
            longestMatch = p;
          }
        }
      }

      if (longestMatch != null) {
        return (ArrayList) rules.get(longestMatch);
      } else {
        return null;
      }
    }
  }
}
