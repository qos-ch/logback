/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.core.issue.lbcore258;


import java.io.IOException;
import java.io.OutputStream;


import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.EncoderBase;
import org.junit.jupiter.api.Test;

/**
 * Provided by Alexander Kudrevatykh in LOGBACK-1362
 */
public class Logback1362 {

    long startNanos = System.nanoTime();
    long DELAY = 20;
    long getNanos() {
        return (System.nanoTime() - startNanos);
    }

    @Test
    public void testAppender() throws InterruptedException {

        OutputStreamAppender<Object> appender = new OutputStreamAppender<Object>() {
            @Override
            public void addError(String msg, Throwable ex) {
                throw new RuntimeException(getNanos()+"  "+msg, ex);
            }
        };

        appender.setEncoder(new EncoderBase<Object>() {

            @Override
            public byte[] headerBytes() {
                return null;
            }

            @Override
            public byte[] encode(Object event) {
                delay(DELAY*2);
                return new byte[]{'A'};
            }

            @Override
            public byte[] footerBytes() {
                // TODO Auto-generated method stub
                return null;
            }
        });
        appender.setOutputStream(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                throw new RuntimeException("not closed appender");
            }
        });

        System.out.println(getNanos() + " About to call appender.start()");
        appender.start();
        System.out.println(getNanos() + " After call to appender.start()");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                delay(DELAY);
                System.out.println(getNanos() + " About to call appender.stop()");
                appender.stop();
                System.out.println(getNanos() + " After call to appender.stop()");
            }
        });
        t.start();
        System.out.println(getNanos() + " Calling appender.doAppend(new Object());");
        appender.doAppend(new Object());
        System.out.println("xxxxxxxxxxxxxxxxxxxxxx");
        System.out.println(getNanos()+ " After call to appender.doAppender(new Object())");
        t.join();
    }

    private void delay(long delayInMillis) {
        try {
            Thread.sleep(delayInMillis);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

