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
package ch.qos.logback.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class EnvUtil {

    static private boolean isJDK_N_OrHigher(int n) {
        List<String> versionList = new ArrayList<String>();
        // this code should work at least until JDK 10 (assuming n parameter is
        // always 6 or more)
        for (int i = 0; i < 5; i++) {
            versionList.add("1." + (n + i));
        }

        String javaVersion = System.getProperty("java.version");
        if (javaVersion == null) {
            return false;
        }
        for (String v : versionList) {
            if (javaVersion.startsWith(v))
                return true;
        }
        return false;
    }

    static public boolean isJDK5() {
        return isJDK_N_OrHigher(5);
    }

    static public boolean isJDK6OrHigher() {
        return isJDK_N_OrHigher(6);
    }

    static public boolean isJDK7OrHigher() {
        return isJDK_N_OrHigher(7);
    }

    static public boolean isJaninoAvailable() {
        ClassLoader classLoader = EnvUtil.class.getClassLoader();
        try {
            Class<?> bindingClass = classLoader.loadClass("org.codehaus.janino.ScriptEvaluator");
            return (bindingClass != null);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os.startsWith("Windows");
    }

}
