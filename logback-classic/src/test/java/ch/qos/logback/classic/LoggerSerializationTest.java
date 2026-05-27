/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.net.server.HardenedLoggingEventInputStream;
import ch.qos.logback.core.net.HardenedObjectInputStream;
import ch.qos.logback.core.testUtil.CoreTestConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoggerSerializationTest {

    static final String SERIALIZATION_PREFIX = CoreTestConstants.TEST_INPUT_PREFIX + "/serialization/";

    // force SLF4J initialization for subsequent Logger readResolve operation
    org.slf4j.Logger unused = LoggerFactory.getLogger(this.getClass());
    LoggerContext loggerContext;
    Logger logger;

    ByteArrayOutputStream bos;
    ObjectOutputStream oos;
    HardenedLoggingEventInputStream hardenedLoggingEventInputStream;
    List<String> whitelist = new ArrayList<String>();

    @BeforeEach
    public void setUp() throws Exception {
        loggerContext = new LoggerContext();
        loggerContext.setName("testContext");
        logger = loggerContext.getLogger(LoggerSerializationTest.class);
        // create the byte output stream
        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
        whitelist.add(Foo.class.getName());
    }

    @AfterEach
    public void tearDown() throws Exception {
        loggerContext = null;
        logger = null;
    }

    @Test
    public void basicSerialization() throws IOException, ClassNotFoundException {
        Foo foo = new Foo(logger);
        foo.doFoo();
        Foo fooBack = writeAndRead(foo);
        fooBack.doFoo();
    }

    @Test
    public void deepTreeSerialization() throws IOException {
        // crate a tree of loggers under "aaaaaaaa"
        Logger a = loggerContext.getLogger("aaaaaaaa");
        loggerContext.getLogger("aaaaaaaa.a");
        loggerContext.getLogger("aaaaaaaa.a.a");
        loggerContext.getLogger("aaaaaaaa.a.b");
        loggerContext.getLogger("aaaaaaaa.a.c");
        loggerContext.getLogger("aaaaaaaa.a.d");

        loggerContext.getLogger("aaaaaaaa.b");
        loggerContext.getLogger("aaaaaaaa.b.a");
        loggerContext.getLogger("aaaaaaaa.b.b");
        loggerContext.getLogger("aaaaaaaa.b.c");
        loggerContext.getLogger("aaaaaaaa.b.d");

        loggerContext.getLogger("aaaaaaaa.c");
        loggerContext.getLogger("aaaaaaaa.c.a");
        loggerContext.getLogger("aaaaaaaa.c.b");
        loggerContext.getLogger("aaaaaaaa.c.c");
        loggerContext.getLogger("aaaaaaaa.c.d");

        loggerContext.getLogger("aaaaaaaa.d");
        loggerContext.getLogger("aaaaaaaa.d.a");
        loggerContext.getLogger("aaaaaaaa.d.b");
        loggerContext.getLogger("aaaaaaaa.d.c");
        loggerContext.getLogger("aaaaaaaa.d.d");

        Logger b = loggerContext.getLogger("b");

        writeObject(oos, a);
        oos.close();
        int sizeA = bos.size();

        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);

        writeObject(oos, b);
        oos.close();
        int sizeB = bos.size();

        assertTrue(sizeA < 100, "serialized logger should be less than 100 bytes");
        // logger tree should not influnce serialization
        assertTrue((sizeA - sizeB) < 10,
                "serialized loggers should be nearly the same size a:" + sizeA + ", sizeB:" + sizeB);
    }

    private Foo writeAndRead(Foo foo) throws IOException, ClassNotFoundException {
        writeObject(oos, foo);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        hardenedLoggingEventInputStream = new HardenedLoggingEventInputStream(loggerContext, bis, whitelist);
        Foo fooBack = readFooObject(hardenedLoggingEventInputStream);
        hardenedLoggingEventInputStream.close();
        return fooBack;
    }

    Foo readFooObject(HardenedObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        return (Foo) readObject(inputStream);
    }

    private Object readObject(HardenedObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        return inputStream.readObject();
    }

    private void writeObject(ObjectOutputStream oos, Object o) throws IOException {
        oos.writeObject(o);
        oos.flush();
        oos.close();
    }

    @Test
    public void testCompatibilityWith_v1_0_11() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(SERIALIZATION_PREFIX + "logger_v1.0.11.ser");
        HardenedObjectInputStream ois = new HardenedLoggingEventInputStream(loggerContext, fis); // new String[]
        // {Logger.class.getName(),
        // LoggerRemoteView.class.getName()});
        Logger a = (Logger) ois.readObject();
        ois.close();
        assertEquals("a", a.getName());
    }

    // interestingly enough, logback 1.0.11 and earlier can also read loggers
    // serialized by 1.0.12.
    // fields not serialized are set to their default values and since the fields
    // are not
    // used, it works out nicely
    @Test
    public void testCompatibilityWith_v1_0_12() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(SERIALIZATION_PREFIX + "logger_v1.0.12.ser");
        HardenedObjectInputStream ois = new HardenedObjectInputStream(loggerContext, fis, new String[]{Logger.class.getName()});
        Logger a = (Logger) ois.readObject();
        ois.close();
        assertEquals("a", a.getName());
    }

}
