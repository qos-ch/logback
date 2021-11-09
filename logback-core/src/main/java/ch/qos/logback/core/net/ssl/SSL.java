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
package ch.qos.logback.core.net.ssl;

/**
 * Various constants used by the SSL implementation.
 *
 * @author Carl Harris
 */
public interface SSL {

    /** Default secure transport protocol */
    String DEFAULT_PROTOCOL = "SSL";

    /** Default key store type */
    String DEFAULT_KEYSTORE_TYPE = "JKS";

    /** Default key store passphrase */
    String DEFAULT_KEYSTORE_PASSWORD = "changeit";

    /** Default secure random generator algorithm */
    String DEFAULT_SECURE_RANDOM_ALGORITHM = "SHA1PRNG";

}
