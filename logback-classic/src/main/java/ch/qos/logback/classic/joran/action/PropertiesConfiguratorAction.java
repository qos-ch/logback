/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.classic.joran.action;

import ch.qos.logback.classic.model.PropertiesConfiguratorModel;
import ch.qos.logback.core.joran.action.ResourceAction;

/**
 * Build an {@link PropertiesConfiguratorModel} instance from SAX events.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.5.8
 */
public class PropertiesConfiguratorAction extends ResourceAction {

    protected PropertiesConfiguratorModel makeNewResourceModel() {
        return new PropertiesConfiguratorModel();
    }

}