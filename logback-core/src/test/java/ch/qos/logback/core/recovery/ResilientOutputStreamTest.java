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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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

    @BeforeAll
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
        assertEquals(1, spy.getCount());

        spy.getChannel().close();
        spy.write("b".getBytes());
        spy.flush();
        // we have 2 in our countingoutput stream
        // but the 'b' write failed due to the channel closing
        assertEquals(2, spy.getCount());
        Thread.sleep(RecoveryCoordinator.BACKOFF_COEFFICIENT_MIN + 10);
        spy.write("c".getBytes());
        spy.flush();

        // since we recovered the output stream, we recomputed
        // our count from the length of the file. both b and c were lost.
        assertEquals(1, spy.getCount());
        verify(spy).openNewOutputStream();

        Thread.sleep(RecoveryCoordinator.BACKOFF_COEFFICIENT_MIN + 10);
        spy.write("d".getBytes());
        spy.flush();
        // the 'd' write succeeds, so we have 2 bytes written
        assertEquals(2, spy.getCount());
    }

}
