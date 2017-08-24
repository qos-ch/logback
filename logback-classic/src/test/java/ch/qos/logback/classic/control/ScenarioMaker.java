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
package ch.qos.logback.classic.control;

import java.util.LinkedList;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.CoreConstants;

public class ScenarioMaker {

    private final static int AVERAGE_LOGGER_DEPTH = 4;
    private final static int LOGGER_DEPT_DEV = 2;
    // the frequency of a set levelInt event for every create logger event
    private final static int CREATE_LOGGER_TO_SET_LEVEL_FREQUENCY = 5;
    private final static int SECOND_SET_LEVEL_FREQUENCY = 3;

    private static long count = 0;

    /**
     * Makes a scenario with len logger creations. Logger names are generated
     * independently such that the overwhelming majority of logger names will be
     * unrelated to each other. Each logger creation may be followed with a
     * randomly generated set levelInt action on that logger.
     * 
     * @param len
     * @return
     */
    static public Scenario makeTypeAScenario(int len) {
        Scenario scenario = new Scenario();
        ;
        for (int i = 0; i < len; i++) {
            String loggerName = ScenarioRandomUtil.randomLoggerName(AVERAGE_LOGGER_DEPTH, LOGGER_DEPT_DEV);
            scenario.add(new CreateLogger(loggerName));
        }
        return scenario;
    }

    static public Scenario makeRealisticCreationScenario(int len) {
        Scenario scenario = new Scenario();
        LinkedList<String> queue = new LinkedList<String>();
        int loggerCreationCount = 0;

        // add an empty string to get going
        queue.add("");

        while (loggerCreationCount < len) {
            if ((count % 100) == 0) {
                System.out.println("count=" + count);
            }

            String loggerName = (String) queue.removeFirst();
            int randomChildrenCount = ScenarioRandomUtil.randomChildrenCount(loggerName);

            if (randomChildrenCount == 0) {
                scenario.add(new CreateLogger(loggerName));
                addSetLevelSubScenario(scenario, loggerName);
                loggerCreationCount++;
            } else {
                for (int i = 0; i < randomChildrenCount; i++) {
                    String childName;
                    if (loggerName.equals("")) {
                        childName = ScenarioRandomUtil.randomId();
                        count += childName.length();
                    } else {
                        childName = loggerName + CoreConstants.DOT + ScenarioRandomUtil.randomId();
                        count += childName.length();
                    }
                    queue.add(childName);
                    addSetLevelSubScenario(scenario, loggerName);
                    loggerCreationCount++;
                }
            }
        }
        return scenario;
    }

    static void addSetLevelSubScenario(Scenario scenario, String loggerName) {
        if (ScenarioRandomUtil.oneInFreq(CREATE_LOGGER_TO_SET_LEVEL_FREQUENCY)) {
            Level l = ScenarioRandomUtil.randomLevel();
            scenario.add(new SetLevel(l, loggerName));
            if (ScenarioRandomUtil.oneInFreq(SECOND_SET_LEVEL_FREQUENCY)) {
                l = ScenarioRandomUtil.randomLevel();
                scenario.add(new SetLevel(l, loggerName));
            }
        }
    }

}