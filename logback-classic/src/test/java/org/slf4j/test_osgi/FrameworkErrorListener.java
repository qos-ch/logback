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
package org.slf4j.test_osgi;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

public class FrameworkErrorListener implements FrameworkListener {

    public List errorList = new ArrayList();

    @SuppressWarnings("unchecked")
    public void frameworkEvent(FrameworkEvent fe) {
        if (fe.getType() == FrameworkEvent.ERROR) {
            errorList.add(fe);
        }
    }

    private void dump(FrameworkEvent fe) {
        Throwable t = fe.getThrowable();
        String tString = null;
        if (t != null) {
            tString = t.toString();
        }
        System.out.println("Framework ERROR:" + ", source " + fe.getSource() + ", bundle=" + fe.getBundle() + ", ex=" + tString);
        if (t != null) {
            t.printStackTrace();
        }
    }

    public void dumpAll() {
        for (int i = 0; i < errorList.size(); i++) {
            FrameworkEvent fe = (FrameworkEvent) errorList.get(i);
            dump(fe);
        }
    }
}
