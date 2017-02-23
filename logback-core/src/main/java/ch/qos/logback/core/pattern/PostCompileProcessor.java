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
package ch.qos.logback.core.pattern;

import ch.qos.logback.core.Context;

/**
 * Implements this to perform post compile processing for a PatternLayout.
 * 
 * For example, PatternLayout in the classic module should add a converter for
 * exception handling (otherwise exceptions would not be printed).
 * 
 * @author Ceki Gulcu
 */
public interface PostCompileProcessor<E> {

    /**
     * Post compile processing of the converter chain.
     * 
     * @param head
     *                The first converter in the chain
     */
    void process(Context context, Converter<E> head);
}
