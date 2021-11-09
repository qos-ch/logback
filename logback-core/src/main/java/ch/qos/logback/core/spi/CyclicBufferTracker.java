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

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.helpers.CyclicBuffer;

/**
 * CyclicBufferTracker tracks  {@link CyclicBuffer} instances.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class CyclicBufferTracker<E> extends AbstractComponentTracker<CyclicBuffer<E>> {

    static final int DEFAULT_NUMBER_OF_BUFFERS = 64;

    static final int DEFAULT_BUFFER_SIZE = 256;
    int bufferSize = DEFAULT_BUFFER_SIZE;

    public CyclicBufferTracker() {
        setMaxComponents(DEFAULT_NUMBER_OF_BUFFERS);
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(final int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    protected void processPriorToRemoval(final CyclicBuffer<E> component) {
        component.clear();
    }

    @Override
    protected CyclicBuffer<E> buildComponent(final String key) {
        return new CyclicBuffer<>(bufferSize);
    }

    @Override
    protected boolean isComponentStale(final CyclicBuffer<E> eCyclicBuffer) {
        return false;
    }

    // for testing purposes
    List<String> liveKeysAsOrderedList() {
        return new ArrayList<>(liveMap.keySet());
    }

    List<String> lingererKeysAsOrderedList() {
        return new ArrayList<>(lingerersMap.keySet());

    }

}
