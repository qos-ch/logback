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
package ch.qos.logback.access;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.ViewStatusMessagesServletBase;

public class ViewStatusMessagesServlet extends ViewStatusMessagesServletBase {

    private static final long serialVersionUID = 443878494348593337L;

    @Override
    protected StatusManager getStatusManager(HttpServletRequest req, HttpServletResponse resp) {

        ServletContext sc = getServletContext();
        return (StatusManager) sc.getAttribute(AccessConstants.LOGBACK_STATUS_MANAGER_KEY);

        // if (result != null) {
        // System.out.println("from ServletContext");
        // return result;
        // } else {
        // HttpSession httpSession = req.getSession(true);
        //
        // System.out.println("from httpSession");
        // return (StatusManager) httpSession
        // .getAttribute(AccessConstants.LOGBACK_STATUS_MANAGER_KEY);
        // }
    }

    @Override
    protected String getPageTitle(HttpServletRequest req, HttpServletResponse resp) {
        return "<h2>Status messages for logback-access</h2>\r\n";
    }
}
