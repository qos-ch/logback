/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.core.rolling;

import static ch.qos.logback.core.CoreConstants.MANUAL_URL_PREFIX;

/**
 * <p>{@link SizeAndTimeBasedFNATP} class was renamed as {@link SizeAndTimeBasedFileNamingAndTriggeringPolicy}
 * in version 1.5.8. In version 1.5.16 it was reintroduced to preserve backward compatibility with existing
 * configurations.</p>
 *
 *
 *
 * @since removed in 1.5.8 and reintroduced in 1.5.16
 */

public class SizeAndTimeBasedFNATP<E> extends SizeAndTimeBasedFileNamingAndTriggeringPolicy<E> {


    @Override
    public void start() {
        addWarn("SizeAndTimeBasedFNATP class was renamed as SizeAndTimeBasedFileNamingAndTriggeringPolicy.");
        super.start();
    }


}
