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
package org.slf4j.impl;

import static org.junit.Assert.assertEquals;

import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactoryFriend;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.status.NopStatusListener;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.testUtil.TeeOutputStream;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class InitializationOutputTest {

    int diff = RandomUtil.getPositiveInt();

    TeeOutputStream tee;
    PrintStream original;

    @Before
    public void setUp() {
        original = System.out;
        // tee will output bytes on System out but it will also
        // collect them so that the output can be compared against
        // some expected output data

        // keep the console quiet
        tee = new TeeOutputStream(null);

        // redirect System.out to tee
        System.setOut(new PrintStream(tee));
    }

    @After
    public void tearDown() {
        System.setOut(original);
        System.clearProperty(ClassicConstants.CONFIG_FILE_PROPERTY);
        System.clearProperty(CoreConstants.STATUS_LISTENER_CLASS_KEY);
    }

    @Test
    public void noOutputIfContextHasAStatusListener() {
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, ClassicTestConstants.INPUT_PREFIX + "issue/logback292.xml");
        System.setProperty(CoreConstants.STATUS_LISTENER_CLASS_KEY, NopStatusListener.class.getName());

        LoggerFactoryFriend.reset();
        assertEquals(0, tee.baos.size());
    }

}
