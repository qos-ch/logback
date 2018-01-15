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
package ch.qos.logback.core.spi;

/**
 *
 * This enum represents the possible replies that a filtering component
 * in logback can return. It is used by implementations of both 
 * {@link ch.qos.logback.core.filter.Filter Filter} and
 * ch.qos.logback.classic.turbo.TurboFilter abstract classes.
 * 
 * Based on the order that the FilterReply values are declared,
 * FilterReply.ACCEPT.compareTo(FilterReply.DENY) will return 
 * a positive value.
 *
 * @author S&eacute;bastien Pennec
 */
public enum FilterReply {
    DENY, NEUTRAL, ACCEPT;
}
