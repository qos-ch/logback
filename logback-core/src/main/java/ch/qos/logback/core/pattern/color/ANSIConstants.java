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
package ch.qos.logback.core.pattern.color;

public class ANSIConstants {

    public final static String ESC_START = "\033[";
    public final static String ESC_END = "m";
    public final static String BOLD = "1;";

    public final static String BLACK_FG = "30";
    public final static String RED_FG = "31";
    public final static String GREEN_FG = "32";
    public final static String YELLOW_FG = "33";
    public final static String BLUE_FG = "34";
    public final static String MAGENTA_FG = "35";
    public final static String CYAN_FG = "36";
    public final static String WHITE_FG = "37";
    public final static String DEFAULT_FG = "39";

}
