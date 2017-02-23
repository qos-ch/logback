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
package ch.qos.logback.core.testUtil;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

public class StringListAppender<E> extends AppenderBase<E> {

    Layout<E> layout;
    public List<String> strList = new ArrayList<String>();

    public void start() {
        strList.clear();

        if (layout == null || !layout.isStarted()) {
            return;
        }
        super.start();
    }

    public void stop() {
        super.stop();
    }

    @Override
    protected void append(E eventObject) {
        String res = layout.doLayout(eventObject);
        strList.add(res);
    }

    public Layout<E> getLayout() {
        return layout;
    }

    public void setLayout(Layout<E> layout) {
        this.layout = layout;
    }
}
