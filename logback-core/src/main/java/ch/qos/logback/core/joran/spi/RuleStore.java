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

import java.util.List;

import ch.qos.logback.core.joran.action.Action;

/**
 * 
 * As its name indicates, a RuleStore contains 2-tuples consists of a ElementSelector
 * and an Action.
 * 
 * <p>As a joran configurator goes through the elements in a document, it asks
 * the rule store whether there are rules matching the current pattern by
 * invoking the {@link #matchActions(ElementPath)} method.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public interface RuleStore {

    /**
     * Add a new rule, given by a pattern and a action class (String).
     *
     * @param elementSelector
     * @param actionClassStr
     * @throws ClassNotFoundException
     */
    void addRule(ElementSelector elementSelector, String actionClassStr) throws ClassNotFoundException;

    /**
     * Add a new rule, given by a pattern and an action instance.
     *
     * @param elementSelector
     * @param action
     */
    void addRule(ElementSelector elementSelector, Action action);

    /**
     * Return a list of actions matching a pattern.
     *
     * @param elementPath the path to match for
     * @return list of matching actions
     */
    List<Action> matchActions(ElementPath elementPath);
}
