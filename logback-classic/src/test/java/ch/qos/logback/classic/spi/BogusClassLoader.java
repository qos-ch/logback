/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.spi;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A trivial class loader which throws a NoClassDefFoundError if the requested
 * class name contains the string "Bogus".
 * 
 * @author Ceki Gülcü
 */
public class BogusClassLoader extends URLClassLoader {

  public BogusClassLoader(URL[] urls) {
    super(urls);
  }

  public BogusClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }

  public Class<?> loadClass(String name) throws ClassNotFoundException {
    return loadClass(name, false);
  }

  /**
   * Throw NoClassDefFoundError if the requested class contains the string
   * "Bogus". Otherwise, delegate to super-class.
   */
  protected Class<?> loadClass(String name, boolean resolve)
      throws ClassNotFoundException {

    if (name.contains("Bogus")) {
      throw new NoClassDefFoundError();
    }

    return super.loadClass(name, resolve);
  }
}
