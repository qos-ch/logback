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
package ch.qos.logback.core.sift;

import ch.qos.logback.core.spi.LifeCycle;

/**
 * Implement this interface in order to compute a discriminating value for a
 * given event of type &lt;E&gt;.
 * 
 * <p>The returned value can depend on any data available at the time of the
 * call, including data contained within the currently running thread.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 * @param <E>
 */
public interface Discriminator<E> extends LifeCycle {

    /**
     * Given event 'e' return a discriminating value.
     * 
     * @param e
     * @return
     */
    String getDiscriminatingValue(E e);

    /**
     * The key or variable name under which the discriminating value should be
     * exported into the host environment. 
     *
     * @return
     */
    String getKey();
}
