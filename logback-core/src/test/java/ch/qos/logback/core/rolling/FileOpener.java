/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.rolling;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Keep the file "output/test.log open for 10 seconds so that we can test
 * RollingFileAppender's ability to roll file open by another process.
 * @author Ceki G&uuml;lc&uuml;
 */
public class FileOpener {
    public static void main(String[] args) throws Exception {
        InputStream is = new FileInputStream("output/test.log");
        is.read();
        Thread.sleep(10000);
        is.close();
        System.out.println("Exiting FileOpener");
    }
}
