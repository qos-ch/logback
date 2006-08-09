/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package ch.qos.logback.core.util;

/**
 * Thrown when an error is encountered whilst attempting to set a property
 * using the {@link PropertySetter} utility class.
 * 
 * @author Anders Kristensen
 */
public class PropertySetterException extends Exception {

  private static final long serialVersionUID = -7524690541928503527L;

  public PropertySetterException(String msg) {
    super(msg);
  }
  
  public PropertySetterException(Throwable rootCause)  {
    super(rootCause);
  }
  
  public PropertySetterException(String message, Throwable cause) {
    super(message, cause);
  }
}
