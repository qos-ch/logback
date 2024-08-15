/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.core.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.hook.ShutdownHookBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Given the code executed in a shutdown hook is the last bit of code that is executed
 * before the JVM exits, there is no way to test that a shutdown hook has been removed or
 * is active.
 *
 * As such, this test cannot be automated.
 */
@Disabled
class ContextUtilAddOrReplaceShutdownHookTest {

    Context context = new ContextBase();
    ContextUtil contextUtil = new ContextUtil(context);

    @Test
    public void smoke() {

        contextUtil.addOrReplaceShutdownHook(new HelloShutdownHookHook(2));
        contextUtil.addOrReplaceShutdownHook(new HelloShutdownHookHook(3));
        contextUtil.addOrReplaceShutdownHook(new HelloShutdownHookHook(5));
        // expect to see
        // HelloShutdownHookHook{number=5}
    }

    static class HelloShutdownHookHook extends ShutdownHookBase {

        int number;


        public HelloShutdownHookHook(int number) {
            this.number = number;

        }

        @Override
        public void run() {
            System.out.println(this);
        }

        @Override
        public String toString() {
            return "HelloShutdownHookHook{" + "number=" + number + '}';
        }
    }

}