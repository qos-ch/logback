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

import org.slf4j.Logger;
import org.slf4j.Marker;


/**
 * This class serves as base for adapters or native implementations of logging systems 
 * lacking Marker support. In this implementation, methods taking marker data 
 * simply invoke the corresponding method without the Marker argument, discarding 
 * any marker data passed as argument.
 * 
 * @author Ceki Gulcu
 */
public abstract class MarkerIgnoringBase implements Logger {

  public boolean isDebugEnabled(Marker marker) {
    return isDebugEnabled();
  }

  public void debug(Marker marker, String msg) {
    debug(msg);
  }

  public void debug(Marker marker, String format, Object arg) {
    debug(format, arg);
  }

  public void debug(Marker marker, String format, Object arg1, Object arg2) {
    debug(format, arg1, arg2);
  }

  public void debug(Marker marker, String format, Object[] argArray) {
    debug(format, argArray);
  }

  public void debug(Marker marker, String msg, Throwable t) {
    debug(msg, t);
  }

  public boolean isInfoEnabled(Marker marker) {
    return isInfoEnabled();
  }

  public void info(Marker marker, String msg) {
    info(msg);
  }

  public void info(Marker marker, String format, Object arg) {
    info(format, arg);
  }

  public void info(Marker marker, String format, Object arg1, Object arg2) {
    info(format, arg1, arg2);
  }

  public void info(Marker marker, String format, Object[] argArray) {
    info(format, argArray);
  }

  public void info(Marker marker, String msg, Throwable t) {
    info(msg, t);
  }

  public boolean isWarnEnabled(Marker marker) {
    return isWarnEnabled();
  }

  public void warn(Marker marker, String msg) {
    warn(msg);
  }

  public void warn(Marker marker, String format, Object arg) {
    warn(format, arg);
  }

  public void warn(Marker marker, String format, Object arg1, Object arg2) {
    warn(format, arg1, arg2);
  }

  public void warn(Marker marker, String format, Object[] argArray) {
    warn(format, argArray);
  }

  public void warn(Marker marker, String msg, Throwable t) {
    warn(msg, t);
  }

 
  public boolean isErrorEnabled(Marker marker) {
    return isErrorEnabled();
  }

  public void error(Marker marker, String msg) {
    error(msg);
  }

  public void error(Marker marker, String format, Object arg) {
    error(format, arg);
  }

  public void error(Marker marker, String format, Object arg1, Object arg2) {
    error(format, arg1, arg2);
  }

  public void error(Marker marker, String format, Object[] argArray) {
    error(format, argArray);
  }

  public void error(Marker marker, String msg, Throwable t) {
    error(msg, t);
  }

}
