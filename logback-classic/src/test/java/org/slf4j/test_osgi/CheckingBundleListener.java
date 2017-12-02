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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

public class CheckingBundleListener implements BundleListener {

    List<BundleEvent> eventList = new ArrayList<BundleEvent>();

    public void bundleChanged(BundleEvent be) {
        eventList.add(be);
    }

    private void dump(BundleEvent be) {
        System.out.println("BundleEvent:" + ", source " + be.getSource() + ", bundle=" + be.getBundle() + ", type=" + be.getType());

    }

    public void dumpAll() {
        for (int i = 0; i < eventList.size(); i++) {
            BundleEvent fe = (BundleEvent) eventList.get(i);
            dump(fe);
        }
    }

    boolean exists(String bundleName) {
        for (int i = 0; i < eventList.size(); i++) {
            BundleEvent fe = (BundleEvent) eventList.get(i);
            Bundle b = fe.getBundle();
            System.out.println("===[" + b + "]");
            if (bundleName.equals(b.getSymbolicName())) {
                return true;
            }
        }
        return false;
    }

}
