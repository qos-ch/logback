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
package ch.qos.logback.classic.kryo;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.util.Util;

import java.io.FileInputStream;

public class ReadStream {

    static public void main(String[] args) throws Exception {
        System.setProperty("kryo.unsafe", "false");

        Kryo kryo = new Kryo();
        kryo.register(SomeClass.class);

        Input input = new Input(new FileInputStream("file.bin"));
        SomeClass object1 = kryo.readObject(input, SomeClass.class);
        input.close();

        System.out.println( object1.value);
    }

}