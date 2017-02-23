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

import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * Base implementation of {@link Discriminator} that provides basic lifecycle management
 *
 * @author Tomasz Nurkiewicz
 * @since 3/29/13, 3:28 PM
 */
public abstract class AbstractDiscriminator<E> extends ContextAwareBase implements Discriminator<E> {

    protected boolean started;

    public void start() {
        started = true;
    }

    public void stop() {
        started = false;
    }

    public boolean isStarted() {
        return started;
    }
}
