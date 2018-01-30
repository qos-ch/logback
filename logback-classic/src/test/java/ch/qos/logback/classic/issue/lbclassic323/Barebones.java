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
package ch.qos.logback.classic.issue.lbclassic323;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;

public class Barebones {

    public static void main(String[] args) {
        Context context = new ContextBase();
        for (int i = 0; i < 3; i++) {
            SenderRunnable senderRunnable = new SenderRunnable("" + i);
            context.getScheduledExecutorService().execute(senderRunnable);
        }
        System.out.println("done");
        // System.exit(0);
    }

    static class SenderRunnable implements Runnable {
        String id;

        SenderRunnable(String id) {
            this.id = id;
        }

        public void run() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            System.out.println("SenderRunnable " + id);
        }
    }
}
