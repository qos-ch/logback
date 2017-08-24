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
package chapters.mdc;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * NumberCruncher factors positive integers.
 */
public interface NumberCruncher extends Remote {
    /**
     * Factor a positive integer <code>number</code> and return its
     * <em>distinct</em> factor's as an integer array.
     * */
    int[] factor(int number) throws RemoteException;
}
