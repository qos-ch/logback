/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package chapter7;

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
