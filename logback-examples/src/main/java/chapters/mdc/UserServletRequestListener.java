/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, 2024, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.helpers;

import java.security.Principal;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;

/**
 * A simple servlet request listener that stores the username
 * found in the Principal in the MDC.
 *
 * <p> The value is removed from the MDC once the request has
 * been fully processed (including an error handler servlet).
 *
 * @author Sven Strickroth
 * @author S&eacute;bastien Pennec
 */
@WebListener
public class UserServletRequestListener implements ServletRequestListener {

    private final String USER_KEY = "username";

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        MDC.clear();
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        if (sre.getServletRequest() instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
            Principal principal = request.getUserPrincipal();
            if (principal != null) {
                String username = principal.getName();
                registerUsername(username);
            }
        }
    }

    private void registerUsername(final String username) {
        if (username != null && !username.trim().isEmpty()) {
            MDC.put(USER_KEY, username);
        }
    }
}
