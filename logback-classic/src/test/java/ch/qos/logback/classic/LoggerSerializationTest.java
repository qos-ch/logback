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
package ch.qos.logback.classic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoggerSerializationTest {

  LoggerContext lc;
  Logger logger;

  ByteArrayOutputStream bos;
  ObjectOutputStream oos;
  ObjectInputStream inputStream;

  @Before
  public void setUp() throws Exception {
    lc = new LoggerContext();
    lc.setName("testContext");
    logger = lc.getLogger(LoggerSerializationTest.class);
    // create the byte output stream
    bos = new ByteArrayOutputStream();
    oos = new ObjectOutputStream(bos);
  }

  @After
  public void tearDown() throws Exception {
    lc = null;
    logger = null;
  }
  
  @Test
  public void serialization() throws IOException, ClassNotFoundException {
    Foo foo = new Foo(logger);
    foo.doFoo();
    Foo fooBack = writeAndRead(foo);
    fooBack.doFoo();
  }

  private Foo writeAndRead(Foo foo) throws IOException,
      ClassNotFoundException {
    oos.writeObject(foo);
    oos.flush();
    oos.close();
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    inputStream = new ObjectInputStream(bis);

    Foo fooBack =  (Foo) inputStream.readObject();
    inputStream.close();
    return fooBack;
  }
}
