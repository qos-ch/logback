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
package ch.qos.logback.core.encoder;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NonClosableInputStream extends FilterInputStream {

    NonClosableInputStream(InputStream is) {
        super(is);
    }

    /**
     * The whole point of this input stream is to ignore invocations to close()
     */
    @Override
    public void close() {

    }

    public void realClose() throws IOException {
        super.close();
    }

}
