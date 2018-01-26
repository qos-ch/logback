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
package ch.qos.logback.classic.html;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class XHTMLEntityResolver implements EntityResolver {
 
    // key: public id, value: relative path to DTD file
    static Map<String, String> entityMap = new HashMap<String, String>();

    static {
        entityMap.put("-//W3C//DTD XHTML 1.0 Strict//EN", "/dtd/xhtml1-strict.dtd");
        entityMap.put("-//W3C//ENTITIES Latin 1 for XHTML//EN", "/dtd/xhtml-lat1.ent");
        entityMap.put("-//W3C//ENTITIES Symbols for XHTML//EN", "/dtd/xhtml-symbol.ent");
        entityMap.put("-//W3C//ENTITIES Special for XHTML//EN", "/dtd/xhtml-special.ent");
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        // System.out.println(publicId);
        final String relativePath = (String) entityMap.get(publicId);

        if (relativePath != null) {
            Class<?> clazz = getClass();
            InputStream in = clazz.getResourceAsStream(relativePath);
            if (in == null) {
                return null;
            } else {
                return new InputSource(in);
            }
        } else {
            return null;
        }
    }
}
