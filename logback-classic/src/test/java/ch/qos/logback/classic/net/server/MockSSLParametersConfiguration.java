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
package ch.qos.logback.classic.net.server;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.net.ssl.SSLParametersConfiguration;

/**
 * A mock {@link SSLParametersConfiguration} with instrumentation for
 * unit testing.
 *
 * @author Carl Harris
 */
class MockSSLParametersConfiguration extends SSLParametersConfiguration {

    private boolean contextInjected;

    @Override
    public void setContext(Context context) {
        contextInjected = true;
        super.setContext(context);
    }

    public boolean isContextInjected() {
        return contextInjected;
    }

}
