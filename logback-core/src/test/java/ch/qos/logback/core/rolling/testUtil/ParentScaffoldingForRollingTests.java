/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 *  Copyright (C) 1999-2025, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *     or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.core.rolling.testUtil;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ParentScaffoldingForRollingTest {

    protected EchoEncoder<Object> encoder = new EchoEncoder<Object>();
    protected int diff = RandomUtil.getPositiveInt();
    protected String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/";

    Calendar calendar = Calendar.getInstance();
    protected Context context = new ContextBase();

    protected long currentTime; // initialized in setUp()
    protected List<Future<?>> futureList = new ArrayList<Future<?>>();

    public void setUp() {
        context.setName("test");
        calendar.set(Calendar.MILLISECOND, 333);
        currentTime = 1760822446333L; //calendar.getTimeInMillis();

    }

    protected void add(Future<?> future) {
        if (future == null)
            return;
        if (!futureList.contains(future)) {
            futureList.add(future);
        }
    }

    protected void waitForJobsToComplete() {
        for (Future<?> future : futureList) {
            try {
                future.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                new RuntimeException("unexpected exception while testing", e);
            }
        }
        futureList.clear();
    }
}
