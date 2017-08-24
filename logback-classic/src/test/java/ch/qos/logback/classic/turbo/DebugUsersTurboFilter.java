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
package ch.qos.logback.classic.turbo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.MDC;
import org.slf4j.Marker;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.FilterReply;

/**
 * This class allows output of debug level events to a certain list of users.
 * 
 * If the level passed as a parameter is of level DEBUG, then the "user" value
 * taken from the MDC is checked against the configured user list. When the user
 * belongs to the list, the request is accepted. Otherwise a NEUTRAL response
 * is sent, thus not influencing the filter chain.  
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class DebugUsersTurboFilter extends TurboFilter {

    List<String> userList = new ArrayList<String>();

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if (!level.equals(Level.DEBUG)) {
            return FilterReply.NEUTRAL;
        }
        String user = MDC.get(ClassicConstants.USER_MDC_KEY);
        if (user != null && userList.contains(user)) {
            return FilterReply.ACCEPT;
        }
        return FilterReply.NEUTRAL;
    }

    public void addUser(String user) {
        userList.add(user);
    }

    // test in BasicJoranTest only, to be removed asap.
    public List<String> getUsers() {
        return userList;
    }

}
