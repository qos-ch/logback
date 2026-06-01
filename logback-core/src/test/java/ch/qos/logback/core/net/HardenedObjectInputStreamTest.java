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

package ch.qos.logback.core.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HardenedObjectInputStreamTest {

    ByteArrayOutputStream bos;
    ObjectOutputStream oos;
    HardenedObjectInputStream inputStream;
    Context context = new ContextBase();
    String[] whitelist = new String[] { Innocent.class.getName() };

    @BeforeEach
    public void setUp() throws Exception {
        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void smoke() throws ClassNotFoundException, IOException {
        Innocent innocent = new Innocent();
        innocent.setAnInt(1);
        innocent.setAnInteger(2);
        innocent.setaString("smoke");
        Innocent back = writeAndRead(innocent);
        assertEquals(innocent, back);
    }

    private Innocent writeAndRead(Innocent innocent) throws IOException, ClassNotFoundException {
        writeObject(oos, innocent);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        inputStream = new HardenedObjectInputStream(context, bis, whitelist);
        Innocent fooBack = (Innocent) inputStream.readObject();
        inputStream.close();
        return fooBack;
    }

    private void writeObject(ObjectOutputStream oos, Object o) throws IOException {
        oos.writeObject(o);
        oos.flush();
        oos.close();
    }

    @Test
    public void denialOfService() throws ClassNotFoundException, IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(payload());
        inputStream = new HardenedObjectInputStream(context, bis, whitelist);
        try {
            assertThrows(InvalidClassException.class, () -> inputStream.readObject());
        } finally {
            inputStream.close();
        }
    }

    private byte[] payload() throws IOException {
        Set root = buildEvilHashset();
        writeObject(oos, root);
        return bos.toByteArray();
    }

    private Set buildEvilHashset() {
        Set root = new HashSet();
        Set s1 = root;
        Set s2 = new HashSet();
        for (int i = 0; i < 100; i++) {
            Set t1 = new HashSet();
            Set t2 = new HashSet();
            t1.add("foo"); // make it not equal to t2
            s1.add(t1);
            s1.add(t2);
            s2.add(t1);
            s2.add(t2);
            s1 = t1;
            s2 = t2;
        }
        return root;
    }

    /**
     * Demonstrates that HardenedObjectInputStream.resolveProxyClass works correctly
     * by rejecting deserialization of dynamic proxy classes, even when the interfaces
     * they implement are whitelisted.
     */
    @Test
    public void resolveProxyClassRejectsDynamicProxies() throws Exception {
        ProxyInterface proxy = (ProxyInterface) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ProxyInterface.class},
                new TestInvocationHandler()
        );

        // Serialize the proxy instance
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(proxy);
        }

        // Attempt to deserialize using HardenedObjectInputStream.
        // We deliberately whitelist both the interface and the invocation handler.
        // Despite this, deserialization must fail because resolveProxyClass always
        // throws InvalidClassException for proxy classes.
        String[] whitelist = new String[]{
                ProxyInterface.class.getName(),
                TestInvocationHandler.class.getName()
        };

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        HardenedObjectInputStream hardenedOis = new HardenedObjectInputStream(context, bis, whitelist);

        assertThrows(InvalidClassException.class, hardenedOis::readObject);
        hardenedOis.close();
    }

    /**
     * A marker interface for the dynamic proxy used in the resolveProxyClass test.
     */
    interface ProxyInterface extends Serializable {
        String getMessage();
    }

    /**
     * A serializable InvocationHandler used to create the test dynamic proxy.
     */
    static class TestInvocationHandler implements InvocationHandler, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("getMessage".equals(method.getName())) {
                return "hello from proxy";
            }
            return null;
        }
    }
}
