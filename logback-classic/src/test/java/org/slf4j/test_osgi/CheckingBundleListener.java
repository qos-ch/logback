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

import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

public class CheckingBundleListener implements BundleListener {

    private final List<BundleEvent> eventList = new ArrayList<>();

    @Override
    public void bundleChanged(final BundleEvent be) {
        eventList.add(be);
    }

    private void dump(final BundleEvent be) {
        System.out.println("BundleEvent:, source " + be.getSource() + ", bundle=" + be.getBundle() + ", type=" + be.getType());
    }

    public void dumpAll() {
        eventList.stream().forEach(this::dump);
    }

    boolean exists(final String bundleName) {
        return eventList.stream().map(BundleEvent::getBundle).peek(b -> System.out.println("===[" + b + "]"))
                        .anyMatch(b -> bundleName.equals(b.getSymbolicName()));
    }

}
