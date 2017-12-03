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
package ch.qos.logback.core.recovery;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class ResilientOutputStreamTest {

    int diff = RandomUtil.getPositiveInt();
    Context context = new ContextBase();

    @BeforeClass
    public static void setUp() {
        File file = new File(CoreTestConstants.OUTPUT_DIR_PREFIX);
        file.mkdirs();
    }

    @Test
    public void verifyRecuperationAfterFailure() throws Exception {
        File file = new File(CoreTestConstants.OUTPUT_DIR_PREFIX + "resilient" + diff + ".log");
        ResilientFileOutputStream rfos = new ResilientFileOutputStream(file, true, FileAppender.DEFAULT_BUFFER_SIZE);
        rfos.setContext(context);

        ResilientFileOutputStream spy = spy(rfos);

        spy.write("a".getBytes());
        spy.flush();

        spy.getChannel().close();
        spy.write("b".getBytes());
        spy.flush();
        Thread.sleep(RecoveryCoordinator.BACKOFF_COEFFICIENT_MIN + 10);
        spy.write("c".getBytes());
        spy.flush();
        verify(spy).openNewOutputStream();

    }

}
