/*
 * Copyright (c) 2004-2006 SLF4J.ORG
 * Copyright (c) 2004-2006 QOS.ch
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


package org.apache.log4j;

import junit.framework.TestCase;

/**
 * A class that tests the invocation of the org.apache.log4j.Logger class
 * that belongs to the log4j-bridge package
 *
 * @author S&eacute;bastien Pennec
 * @author Ceki G&uuml;lc&uuml;
 */
public class InvokeLog4jTest extends TestCase {

  public void testIsEnabledAPI() {
    Logger log = Logger.getLogger(InvokeLog4jTest.class.getName());
    
    assertTrue(log.isTraceEnabled());
    assertTrue(log.isDebugEnabled());
    assertTrue(log.isInfoEnabled());
    assertTrue(log.isWarnEnabled());
    assertTrue(log.isErrorEnabled());
  }
  
  public void testPrintAPI() {
    Logger log = Logger.getLogger(InvokeLog4jTest.class.getName());
    Exception e = new Exception("just testing");
    
    log.debug(null);
    log.debug("debug message");
    
    log.info(null);
    log.info("info  message");
    
    log.warn(null);
    log.warn("warn message");

    log.error(null);
    log.error("error message");
    
    log.debug(null, e);
    log.debug("debug message", e);
    
    log.info(null, e);    
    log.info("info  message", e);
    
    log.warn(null, e);
    log.warn("warn message", e);
    
    log.error(null, e);
    log.error("error message", e);
  }
}
