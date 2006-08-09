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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.ILoggerFactory;


/**
 * An implementation of {@link ILoggerFactory} which always returns
 * {@link SimpleLogger} instances.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class SimpleLoggerFactory implements ILoggerFactory {


  /**
   * A default instance of SimpleLoggerFactory. This default instance may be used
   * to retrieve a simple logger as a last-resort fallback logger. This instance 
   * is designed to be used by a very specific group of users, namely for those 
   * developing fully-fledged logging systems (e.g. log4j or logback). It is not 
   * intended for end-users of the SLF4J API.
   */
  public final static SimpleLoggerFactory INSTANCE = new SimpleLoggerFactory();
  
  Map<String, Logger> map;
  
  public SimpleLoggerFactory() {
    map = new HashMap<String, Logger>();
  }


  /**
   * Return an appropriate {@link SimpleLogger} instance by name. 
   */
  public Logger getLogger(String name) {
    Logger slogger = (Logger) map.get(name);
    if(slogger == null) {
      slogger = new SimpleLogger(name);
      map.put(name, slogger);
    }
    return slogger;
  }
}
