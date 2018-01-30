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
package ch.qos.logback.core.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * CyclicBuffer holds values in a cyclic array.
 * 
 * <p>It allows read access to any element in the buffer not just the first or
 * last element.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class CyclicBuffer<E> {

    E[] ea;
    int first;
    int last;
    int numElems;
    int maxSize;

    /**
     * Instantiate a new CyclicBuffer of at most <code>maxSize</code> events.
     * 
     * The <code>maxSize</code> argument must a positive integer.
     * 
     * @param maxSize
     *                The maximum number of elements in the buffer.
     */
    public CyclicBuffer(int maxSize) throws IllegalArgumentException {
        if (maxSize < 1) {
            throw new IllegalArgumentException("The maxSize argument (" + maxSize + ") is not a positive integer.");
        }
        init(maxSize);
    }

    @SuppressWarnings("unchecked")
    public CyclicBuffer(CyclicBuffer<E> other) {
        this.maxSize = other.maxSize;
        ea = (E[]) new Object[maxSize];
        System.arraycopy(other.ea, 0, this.ea, 0, maxSize);
        this.last = other.last;
        this.first = other.first;
        this.numElems = other.numElems;
    }

    @SuppressWarnings("unchecked")
    private void init(int maxSize) {
        this.maxSize = maxSize;
        ea = (E[]) new Object[maxSize];
        first = 0;
        last = 0;
        numElems = 0;
    }

    /**
     * Clears the buffer and resets all attributes.
     */
    public void clear() {
        init(this.maxSize);
    }

    /**
     * Add an <code>event</code> as the last event in the buffer.
     * 
     */
    public void add(E event) {
        ea[last] = event;
        if (++last == maxSize)
            last = 0;

        if (numElems < maxSize)
            numElems++;
        else if (++first == maxSize)
            first = 0;
    }

    /**
     * Get the <i>i</i>th oldest event currently in the buffer. If <em>i</em>
     * is outside the range 0 to the number of elements currently in the buffer,
     * then <code>null</code> is returned.
     */
    public E get(int i) {
        if (i < 0 || i >= numElems)
            return null;

        return ea[(first + i) % maxSize];
    }

    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Get the oldest (first) element in the buffer. The oldest element is removed
     * from the buffer.
     */
    public E get() {
        E r = null;
        if (numElems > 0) {
            numElems--;
            r = ea[first];
            ea[first] = null;
            if (++first == maxSize)
                first = 0;
        }
        return r;
    }

    public List<E> asList() {
        List<E> tList = new ArrayList<E>();
        for (int i = 0; i < length(); i++) {
            tList.add(get(i));
        }
        return tList;
    }

    /**
     * Get the number of elements in the buffer. This number is guaranteed to be
     * in the range 0 to <code>maxSize</code> (inclusive).
     */
    public int length() {
        return numElems;
    }

    /**
     * Resize the cyclic buffer to <code>newSize</code>.
     * 
     * @throws IllegalArgumentException
     *                 if <code>newSize</code> is negative.
     */
    @SuppressWarnings("unchecked")
    public void resize(int newSize) {
        if (newSize < 0) {
            throw new IllegalArgumentException("Negative array size [" + newSize + "] not allowed.");
        }
        if (newSize == numElems)
            return; // nothing to do

        //
        E[] temp = (E[]) new Object[newSize];

        int loopLen = newSize < numElems ? newSize : numElems;

        for (int i = 0; i < loopLen; i++) {
            temp[i] = ea[first];
            ea[first] = null;
            if (++first == numElems)
                first = 0;
        }
        ea = temp;
        first = 0;
        numElems = loopLen;
        maxSize = newSize;
        if (loopLen == newSize) {
            last = 0;
        } else {
            last = loopLen;
        }
    }
}
