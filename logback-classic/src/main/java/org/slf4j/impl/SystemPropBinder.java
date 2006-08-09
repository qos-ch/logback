/*
 * Copyright (c) 2004-2005 SLF4J.ORG
 * Copyright (c) 2004-2005 QOS.ch
 *
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute, and/or sell copies of  the Software, and to permit persons
 * to whom  the Software is furnished  to do so, provided  that the above
 * copyright notice(s) and this permission notice appear in all copies of
 * the  Software and  that both  the above  copyright notice(s)  and this
 * permission notice appear in supporting documentation.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR  A PARTICULAR PURPOSE AND NONINFRINGEMENT
 * OF  THIRD PARTY  RIGHTS. IN  NO EVENT  SHALL THE  COPYRIGHT  HOLDER OR
 * HOLDERS  INCLUDED IN  THIS  NOTICE BE  LIABLE  FOR ANY  CLAIM, OR  ANY
 * SPECIAL INDIRECT  OR CONSEQUENTIAL DAMAGES, OR  ANY DAMAGES WHATSOEVER
 * RESULTING FROM LOSS  OF USE, DATA OR PROFITS, WHETHER  IN AN ACTION OF
 * CONTRACT, NEGLIGENCE  OR OTHER TORTIOUS  ACTION, ARISING OUT OF  OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 * Except as  contained in  this notice, the  name of a  copyright holder
 * shall not be used in advertising or otherwise to promote the sale, use
 * or other dealings in this Software without prior written authorization
 * of the copyright holder.
 *
 */
package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * Allows for dynamic binding as specified by information contained in the
 * {@link #LOGGER_FACTORY_PROPERTY} java system property.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class SystemPropBinder implements LoggerFactoryBinder {

  /**
   * The name of the system property to set in order to instruct
   * {@link org.slf4j.LoggerFactory} class to use a specific ILoggerFactory.
   */
  final public static String LOGGER_FACTORY_PROPERTY = "org.slf4j.factory";

  /**
   * Constant used to determine the name of the factory method for creating
   * logger factories.
   */
  final public static String LOGGER_FACTORY_FACTORY_METHOD_NAME = "getInstance";

  String factoryFactoryClassName = null;

  /**
   * Fetch the appropriate ILoggerFactory as instructed by the system
   * properties.
   * 
   * 
   * @return The appropriate ILoggerFactory instance as directed from the system
   *         properties
   */
  public ILoggerFactory getLoggerFactory() {

    try {
      if (getLoggerFactoryClassStr() == null) {
        return null;
      }

      Class factoryFactoryClass = Class.forName(getLoggerFactoryClassStr());
      Class[] EMPTY_CLASS_ARRAY = {};
      java.lang.reflect.Method factoryFactoryMethod = factoryFactoryClass
          .getDeclaredMethod(LOGGER_FACTORY_FACTORY_METHOD_NAME,
              EMPTY_CLASS_ARRAY);
      ILoggerFactory loggerFactory = (ILoggerFactory) factoryFactoryMethod
          .invoke(null, null);
      return loggerFactory;
    } catch (Exception e) {
      Util.reportFailure("Failed to fetch ILoggerFactory instnace using the "
          + factoryFactoryClassName + " class.", e);

    }

    // we could not get an adapter
    return null;
  }

  public String getLoggerFactoryClassStr() {
    if (factoryFactoryClassName == null) {
      try {
        factoryFactoryClassName = System.getProperty(LOGGER_FACTORY_PROPERTY);
      } catch (Exception e) {
        Util.reportFailure("Failed to fetch " + LOGGER_FACTORY_PROPERTY
            + " system property.", e);
      }
    }
    return factoryFactoryClassName;
  }
}