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
package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.util.OptionHelper;

public class ResponseHeaderConverter extends AccessConverter {

    String key;

    @Override
    public void start() {
        key = getFirstOption();
        if (OptionHelper.isNullOrEmpty(key)) {
            addWarn("Missing key for the response header");
        } else {
            super.start();
        }
    }

    @Override
    public String convert(IAccessEvent accessEvent) {
        if (!isStarted()) {
            return "INACTIVE_REPONSE_HEADER_CONV";
        }

        return accessEvent.getResponseHeader(key);
        // return null;

        // HttpServletResponse response = accessEvent.getHttpResponse();
        //
        // Object value = null; // = response.getHeader(key);
        // if (value == null) {
        // return AccessConverter.NA;
        // } else {
        // return value.toString();
        // }
    }

}
