/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.blackbox.model;

import ch.qos.logback.core.model.NamedModel;

public class BlackboxStackModel extends NamedModel {

    private static final long serialVersionUID = -2623437394373933695L;

    @Override
    protected BlackboxStackModel makeNewInstance() {
        return new BlackboxStackModel();
    }
}
