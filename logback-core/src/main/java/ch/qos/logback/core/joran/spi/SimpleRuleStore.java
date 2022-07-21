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
package ch.qos.logback.core.joran.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

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

    static String KLEENE_STAR = "*";

    // key: Pattern instance, value: ArrayList containing actions
    HashMap<ElementSelector, Supplier<Action>> rules = new HashMap<>();

    List<String> transparentPathParts = new ArrayList<>(2);

    public SimpleRuleStore(Context context) {
        setContext(context);
    }

    public void addTransparentPathPart(String pathPart) {
        if (pathPart == null)
            throw new IllegalArgumentException("pathPart cannot be null");

        pathPart = pathPart.trim();

        if (pathPart.isEmpty())
            throw new IllegalArgumentException("pathPart cannot be empty or to consist of only spaces");

        if (pathPart.contains("/"))
            throw new IllegalArgumentException("pathPart cannot contain '/', i.e. the forward slash character");

        transparentPathParts.add(pathPart);

    }

    /**
     * Add a new rule, i.e. a pattern, action pair to the rule store.
     * <p>
     * Note that the added action's LoggerRepository will be set in the process.
     */
    public void addRule(ElementSelector elementSelector, Supplier<Action> actionSupplier) {

        Supplier<Action> existing = rules.get(elementSelector);

        if (existing == null) {
            rules.put(elementSelector, actionSupplier);
        } else {
            throw new IllegalStateException(elementSelector.toString() + " already has an associated action supplier");
        }
    }

    public void addRule(ElementSelector elementSelector, String actionClassName) {
        Action action = null;

        try {
            action = (Action) OptionHelper.instantiateByClassName(actionClassName, Action.class, context);
        } catch (Exception e) {
            addError("Could not instantiate class [" + actionClassName + "]", e);
        }
        if (action != null) {
     //       addRule(elementSelector, action);
        }
    }

    // exact match has the highest priority
    // if no exact match, check for suffix (tail) match, i.e matches
    // of type */x/y. Suffix match for */x/y has higher priority than match for
    // */x
    // if no suffix match, check for prefix match, i.e. matches for x/*
    // match for x/y/* has higher priority than matches for x/*

    public Supplier<Action> matchActions(ElementPath elementPath) {
        
        Supplier<Action> actionSupplier = internalMatchAction(elementPath);
        if(actionSupplier != null) {
            return actionSupplier;
        } else {
            ElementPath cleanedElementPath = removeTransparentPathParts(elementPath);
            return internalMatchAction(cleanedElementPath);
        }
    }

    private Supplier<Action> internalMatchAction(ElementPath elementPath) {
        Supplier<Action> actionSupplier;

        if ((actionSupplier = fullPathMatch(elementPath)) != null) {
            return actionSupplier;
        } else if ((actionSupplier = suffixMatch(elementPath)) != null) {
            return actionSupplier;
        } else if ((actionSupplier = prefixMatch(elementPath)) != null) {
            return actionSupplier;
        } else if ((actionSupplier = middleMatch(elementPath)) != null) {
            return actionSupplier;
        } else {
            return null;
        }
    }

    ElementPath removeTransparentPathParts(ElementPath originalElementPath) {

        List<String> preservedElementList = new ArrayList<>(originalElementPath.partList.size());

        for (String part : originalElementPath.partList) {
            boolean shouldKeep = transparentPathParts.stream().noneMatch(p -> p.equalsIgnoreCase(part));
            if (shouldKeep)
                preservedElementList.add(part);
        }

        return new ElementPath(preservedElementList);

    }

    Supplier<Action> fullPathMatch(ElementPath elementPath) {
        for (ElementSelector selector : rules.keySet()) {
            if (selector.fullPathMatch(elementPath))
                return rules.get(selector);
        }
        return null;
    }

    // Suffix matches are matches of type */x/y
    Supplier<Action> suffixMatch(ElementPath elementPath) {
        int max = 0;
        ElementSelector longestMatchingElementSelector = null;

        for (ElementSelector selector : rules.keySet()) {
            if (isSuffixPattern(selector)) {
                int r = selector.getTailMatchLength(elementPath);
                if (r > max) {
                    max = r;
                    longestMatchingElementSelector = selector;
                }
            }
        }

        if (longestMatchingElementSelector != null) {
            return rules.get(longestMatchingElementSelector);
        } else {
            return null;
        }
    }

    private boolean isSuffixPattern(ElementSelector p) {
        return (p.size() > 1) && p.get(0).equals(KLEENE_STAR);
    }

    Supplier<Action> prefixMatch(ElementPath elementPath) {
        int max = 0;
        ElementSelector longestMatchingElementSelector = null;

        for (ElementSelector selector : rules.keySet()) {
            String last = selector.peekLast();
            if (isKleeneStar(last)) {
                int r = selector.getPrefixMatchLength(elementPath);
                // to qualify the match length must equal p's size omitting the '*'
                if ((r == selector.size() - 1) && (r > max)) {
                    max = r;
                    longestMatchingElementSelector = selector;
                }
            }
        }

        if (longestMatchingElementSelector != null) {
            return rules.get(longestMatchingElementSelector);
        } else {
            return null;
        }
    }

    private boolean isKleeneStar(String last) {
        return KLEENE_STAR.equals(last);
    }

    Supplier<Action> middleMatch(ElementPath path) {

        int max = 0;
        ElementSelector longestMatchingElementSelector = null;

        for (ElementSelector selector : rules.keySet()) {
            String last = selector.peekLast();
            String first = null;
            if (selector.size() > 1) {
                first = selector.get(0);
            }
            if (isKleeneStar(last) && isKleeneStar(first)) {
                List<String> copyOfPartList = selector.getCopyOfPartList();
                if (copyOfPartList.size() > 2) {
                    copyOfPartList.remove(0);
                    copyOfPartList.remove(copyOfPartList.size() - 1);
                }

                int r = 0;
                ElementSelector clone = new ElementSelector(copyOfPartList);
                if (clone.isContainedIn(path)) {
                    r = clone.size();
                }
                if (r > max) {
                    max = r;
                    longestMatchingElementSelector = selector;
                }
            }
        }

        if (longestMatchingElementSelector != null) {
            return rules.get(longestMatchingElementSelector);
        } else {
            return null;
        }
    }

    public String toString() {
        final String TAB = "  ";

        StringBuilder retValue = new StringBuilder();

        retValue.append("SimpleRuleStore ( ").append("rules = ").append(this.rules).append(TAB).append(" )");

        return retValue.toString();
    }

}
