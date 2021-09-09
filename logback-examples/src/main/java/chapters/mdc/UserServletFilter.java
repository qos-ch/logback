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
package chapters.mdc;

import java.io.IOException;
import java.security.Principal;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.MDC;

/**
 * A simple servlet filter that puts the username
 * found either in the Principal.
 * 
 * <p> The value is removed from the MDC once the request has been
 * fully processed.
 *
 * @author S&eacute;bastien Pennec
 */
public class UserServletFilter implements Filter {

    private final String USER_KEY = "username";

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        boolean successfulRegistration = false;
        HttpServletRequest req = (HttpServletRequest) request;
        Principal principal = req.getUserPrincipal();
        // Please note that we also could have used a cookie to
        // retrieve the user name

        if (principal != null) {
            String username = principal.getName();
            successfulRegistration = registerUsername(username);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            if (successfulRegistration) {
                MDC.remove(USER_KEY);
            }
        }
    }

    public void init(FilterConfig arg0) throws ServletException {
    }

    /**
     * Register the user in the MDC under USER_KEY.
     * 
     * @param username
     * @return true id the user can be successfully registered
     */
    private boolean registerUsername(String username) {
        if (username != null && username.trim().length() > 0) {
            MDC.put(USER_KEY, username);
            return true;
        }
        return false;
    }

}
