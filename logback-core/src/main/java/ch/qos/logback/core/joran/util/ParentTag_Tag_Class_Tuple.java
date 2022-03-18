/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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

package ch.qos.logback.core.joran.util;


/**
 * 
 * Each tuple represents the association of a parentTag, tag, and a className 
 * which corresponds to default class name rules.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.3.0-alpha15
 *
 */
public class ParentTag_Tag_Class_Tuple {
    public final String parentTag;
    public final String tag;
    public final String className;

    public ParentTag_Tag_Class_Tuple(String parentTag, String tag, String className) {
        super();
        this.parentTag = parentTag;
        this.tag = tag;
        this.className = className;
    }

    @Override
    public String toString() {
        return "ParentTag_Tag_Class_Tuple [parentTag=" + parentTag + ", tag=" + tag + ", className=" + className + "]";
    }
}
