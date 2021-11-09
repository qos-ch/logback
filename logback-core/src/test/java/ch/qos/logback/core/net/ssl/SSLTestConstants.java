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
 * Constants used by unit tests in this package.
 *
 * @author Carl Harris
 */
public interface SSLTestConstants {

    String KEYSTORE_JKS_RESOURCE = "net/ssl/keystore.jks";

    String KEYSTORE_PKCS12_RESOURCE = "net/ssl/keystore.p12";

    String PKCS12_TYPE = "PKCS12";

    String FAKE_ALGORITHM_NAME = "A_FAKE_ALGORITHM_NAME";

    String FAKE_PROVIDER_NAME = "A_FAKE_PROVIDER_NAME";

}
