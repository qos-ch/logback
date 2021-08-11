/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2021, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.testUtil;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is duplicated in slf4j-api-testsjar since 2.0.0-alpha4.
 * 
 * Use the copy in since 2.0.0-alpha4-tests.jar once it is released.
 * 
 * @author Ceki
 *
 */
public class StringPrintStream extends PrintStream {

    public static final String LINE_SEP = System.getProperty("line.separator");
    PrintStream other;
    boolean duplicate = false;

    public List<String> stringList = Collections.synchronizedList(new ArrayList<String>());

    public StringPrintStream(PrintStream ps, boolean duplicate) {
        super(ps);
        other = ps;
        this.duplicate = duplicate;
    }

    public StringPrintStream(PrintStream ps) {
        this(ps, false);
    }

    public void print(String s) {
        if (duplicate)
            other.print(s);
        stringList.add(s);
    }

    public void println(String s) {
        if (duplicate)
            other.println(s);
        stringList.add(s);
    }

    public void println(Object o) {
        if (duplicate)
            other.println(o);
        stringList.add(o.toString());
    }
}