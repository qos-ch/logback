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
package ch.qos.logback.access.servlet;

import ch.qos.logback.access.AccessConstants;

public class Util {
    private Util() {
    }

    public static boolean isFormUrlEncoded(String contentType, String method) {
        return "POST".equalsIgnoreCase(method) && contentType != null && contentType.startsWith(AccessConstants.X_WWW_FORM_URLECODED);
    }

    public static boolean isImageResponse(String contentType) {
        return contentType != null && contentType.startsWith(AccessConstants.IMAGE_CONTENT_TYPE);
    }
}
