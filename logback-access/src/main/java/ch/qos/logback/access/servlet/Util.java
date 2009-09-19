/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.access.AccessConstants;

public class Util {

  public static boolean isFormUrlEncoded(HttpServletRequest request) {
    if ("POST".equals(request.getMethod())
        && AccessConstants.X_WWW_FORM_URLECODED.equals(request.getContentType())) {
      return true;
    } else {
      return false;
    }
  }
  
  public static boolean isImageResponse(HttpServletResponse response) {
    
    String responseType = response.getContentType();
    
    if (responseType != null && responseType.startsWith(AccessConstants.IMAGE_CONTENT_TYPE)) {
      return true;
    } else {
      return false;
    }
  }
}
