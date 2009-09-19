/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.makeThreadSafe;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Test;

import ch.qos.logback.core.Appender;

/**
 * This test shows the general problem I described in LBCORE-67.
 * 
 * In the two test cases below, an appender that throws an OutOfMemoryError
 * while getName is called - but this is just an example to show the general
 * problem.
 * 
 * The tests below fail without fixing LBCORE-67 and pass when Joern Huxhorn's
 * patch is applied.
 * 
 * Additionally, the following, probably more realistic, situations could
 * happen:
 * 
 * -addAppender: appenderList.add() could throw OutOfMemoryError. This could
 * only be shown by using an appenderList mock but appenderList does not (and
 * should not) have a setter. This would leave the write lock locked.
 * 
 * -iteratorForAppenders: new ArrayList() could throw an OutOfMemoryError,
 * leaving the read lock locked.
 * 
 * I can't imagine a bad situation in isAttached, detachAppender(Appender) or
 * detachAppender(String) but I'd change the code anyway for consistency. I'm
 * also pretty sure that something stupid can happen at any time so it's best to
 * just stick to conventions.
 * 
 * @author Joern Huxhorn
 */
public class AppenderAttachableImplLockTest {
  private AppenderAttachableImpl<Integer> aai = new AppenderAttachableImpl<Integer>();

  @SuppressWarnings("unchecked")
  @Test(timeout = 1000)
  public void getAppenderBoom() {

    Appender<Integer> mockAppender1 = createStrictMock(Appender.class);
    expect(mockAppender1.getName()).andThrow(new OutOfMemoryError("oops"));
    replay(mockAppender1);

    aai.addAppender(mockAppender1);
    try {
      // appender.getName called as a result of next statement
      aai.getAppender("foo");
    } catch (OutOfMemoryError e) {
      // this leaves the read lock locked.
    }

    Appender<Integer> mockAppender2=createStrictMock(Appender.class);
    // the next call will lock
     aai.addAppender(mockAppender2);
     verify(mockAppender1);
  }

  @SuppressWarnings("unchecked")
  @Test(timeout = 1000)
  public void detachAppenderBoom() throws InterruptedException {
    Appender<Integer> mockAppender = createStrictMock(Appender.class);
    makeThreadSafe(mockAppender, true);
    expect(mockAppender.getName()).andThrow(new OutOfMemoryError("oops"));
    mockAppender.doAppend(17);
    replay(mockAppender);

    aai.addAppender(mockAppender);
    Thread t = new Thread(new Runnable() {

      public void run() {
        try {
          // appender.getName called as a result of next statement
          aai.detachAppender("foo");
        } catch (OutOfMemoryError e) {
          // this leaves the write lock locked.
        }
      }
    });
    t.start();
    t.join();

    // the next call will lock
    aai.appendLoopOnAppenders(17);
    verify(mockAppender);
  }

}
