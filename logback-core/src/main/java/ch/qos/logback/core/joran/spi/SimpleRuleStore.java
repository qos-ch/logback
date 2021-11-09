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
	HashMap<ElementSelector, List<Action>> rules = new HashMap<>();

	// public SimpleRuleStore() {
	// }

	public SimpleRuleStore(final Context context) {
		setContext(context);
	}

	/**
	 * Add a new rule, i.e. a pattern, action pair to the rule store. <p> Note
	 * that the added action's LoggerRepository will be set in the process.
	 */
	@Override
	public void addRule(final ElementSelector elementSelector, final Action action) {
		action.setContext(context);

		List<Action> a4p = rules.get(elementSelector);

		if (a4p == null) {
			a4p = new ArrayList<>();
			rules.put(elementSelector, a4p);
		}

		a4p.add(action);
	}

	@Override
	public void addRule(final ElementSelector elementSelector, final String actionClassName) {
		Action action = null;

		try {
			action = (Action) OptionHelper.instantiateByClassName(actionClassName, Action.class, context);
		} catch (final Exception e) {
			addError("Could not instantiate class [" + actionClassName + "]", e);
		}
		if (action != null) {
			addRule(elementSelector, action);
		}
	}

	// exact match has highest priority
	// if no exact match, check for suffix (tail) match, i.e matches
	// of type */x/y. Suffix match for */x/y has higher priority than match for
	// */x
	// if no suffix match, check for prefix match, i.e. matches for x/*
	// match for x/y/* has higher priority than matches for x/*

	@Override
	public List<Action> matchActions(final ElementPath elementPath) {
		List<Action> actionList;

		if ((actionList = fullPathMatch(elementPath)) != null) {
			return actionList;
		}
		if (((actionList = suffixMatch(elementPath)) != null) || ((actionList = prefixMatch(elementPath)) != null) || ((actionList = middleMatch(elementPath)) != null)) {
			return actionList;
		} else {
			return null;
		}
	}

	List<Action> fullPathMatch(final ElementPath elementPath) {
		for (final ElementSelector selector : rules.keySet()) {
			if (selector.fullPathMatch(elementPath)) {
				return rules.get(selector);
			}
		}
		return null;
	}

	// Suffix matches are matches of type */x/y
	List<Action> suffixMatch(final ElementPath elementPath) {
		int max = 0;
		ElementSelector longestMatchingElementSelector = null;

		for (final ElementSelector selector : rules.keySet()) {
			if (isSuffixPattern(selector)) {
				final int r = selector.getTailMatchLength(elementPath);
				if (r > max) {
					max = r;
					longestMatchingElementSelector = selector;
				}
			}
		}

		if (longestMatchingElementSelector != null) {
			return rules.get(longestMatchingElementSelector);
		}
		return null;
	}

	private boolean isSuffixPattern(final ElementSelector p) {
		return p.size() > 1 && p.get(0).equals(KLEENE_STAR);
	}

	List<Action> prefixMatch(final ElementPath elementPath) {
		int max = 0;
		ElementSelector longestMatchingElementSelector = null;

		for (final ElementSelector selector : rules.keySet()) {
			final String last = selector.peekLast();
			if (isKleeneStar(last)) {
				final int r = selector.getPrefixMatchLength(elementPath);
				// to qualify the match length must equal p's size omitting the '*'
				if (r == selector.size() - 1 && r > max) {
					max = r;
					longestMatchingElementSelector = selector;
				}
			}
		}

		if (longestMatchingElementSelector != null) {
			return rules.get(longestMatchingElementSelector);
		}
		return null;
	}

	private boolean isKleeneStar(final String last) {
		return KLEENE_STAR.equals(last);
	}

	List<Action> middleMatch(final ElementPath path) {

		int max = 0;
		ElementSelector longestMatchingElementSelector = null;

		for (final ElementSelector selector : rules.keySet()) {
			final String last = selector.peekLast();
			String first = null;
			if (selector.size() > 1) {
				first = selector.get(0);
			}
			if (isKleeneStar(last) && isKleeneStar(first)) {
				final List<String> copyOfPartList = selector.getCopyOfPartList();
				if (copyOfPartList.size() > 2) {
					copyOfPartList.remove(0);
					copyOfPartList.remove(copyOfPartList.size() - 1);
				}

				int r = 0;
				final ElementSelector clone = new ElementSelector(copyOfPartList);
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
		}
		return null;
	}

	@Override
	public String toString() {
		final String TAB = "  ";

		final StringBuilder retValue = new StringBuilder();

		retValue.append("SimpleRuleStore ( ").append("rules = ").append(rules).append(TAB).append(" )");

		return retValue.toString();
	}

}
