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
package ch.qos.logback.core.spi;

import ch.qos.logback.core.Appender;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This test shows the general problem I described in LOGBACK-102.
 *
 * In the two test cases below, an appender that throws an RuntimeException
 * while getName is called - but this is just an example to show the general
 * problem.
 *
 * The tests below fail without fixing LBCORE-67 and pass when Joern Huxhorn's
 * patch is applied.
 *
 * Additionally, the following, probably more realistic, situations could
 * happen:
 *
 * -addAppender: appenderList.add() could throw OutOfMemoryError. This could
 * only be shown by using an appenderList mock but appenderList does not (and
 * should not) have a setter. This would leave the write lock locked.
 *
 * -iteratorForAppenders: new ArrayList() could throw an OutOfMemoryError,
 * leaving the read lock locked.
 *
 * I can't imagine a bad situation in isAttached, detachAppender(Appender) or
 * detachAppender(String) but I'd change the code anyway for consistency. I'm
 * also pretty sure that something stupid can happen at any time so it's best to
 * just stick to conventions.
 *
 * @author Joern Huxhorn
 */
public class AppenderAttachableImplLockTest {

    private AppenderAttachableImpl<Integer> aai = new AppenderAttachableImpl<Integer>();

    @SuppressWarnings("unchecked")
    @Test(timeout = 1000)
    public void getAppenderBoom() {
        Appender<Integer> mockAppender1 = mock(Appender.class);

        when(mockAppender1.getName()).thenThrow(new RuntimeException("oops"));
        aai.addAppender(mockAppender1);
        try {
            // appender.getName called as a result of next statement
            aai.getAppender("foo");
        } catch (RuntimeException e) {
            // this leaves the read lock locked.
        }

        Appender<Integer> mockAppender2 = mock(Appender.class);
        // the next call used to freeze with the earlier ReadWriteLock lock
        // implementation
        aai.addAppender(mockAppender2);
    }

    @SuppressWarnings("unchecked")
    @Test(timeout = 15000)
    //@Test
    public void detachAppenderBoom() throws InterruptedException {
        Appender<Integer> mockAppender = mock(Appender.class);
        when(mockAppender.getName()).thenThrow(new RuntimeException("oops"));
        mockAppender.doAppend(17);

        aai.addAppender(mockAppender);
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    // appender.getName called as a result of next statement
                    aai.detachAppender("foo");
                } catch (RuntimeException e) {
                	System.out.println("Caught "+e.toString());
                    // this leaves the write lock locked.
                }
            }
        });
        t.start();
        t.join();

        // the next call used to freeze with the earlier ReadWriteLock lock
        // implementation
        aai.appendLoopOnAppenders(17);
    }
}
