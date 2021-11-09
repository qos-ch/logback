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
package ch.qos.logback.core.joran.spi;

import java.util.Objects;

/**
 * A 2-tuple (a double) consisting of a Class and a String. The Class references
 * the hosting class of a component and the String represents the property name
 * under which a nested component is referenced the host.
 *
 * This class is used by {@link DefaultNestedComponentRegistry}.
 *
 * @author Ceki Gulcu
 *
 */
public class HostClassAndPropertyDouble {

    final Class<?> hostClass;
    final String propertyName;

    public HostClassAndPropertyDouble(final Class<?> hostClass, final String propertyName) {
        this.hostClass = hostClass;
        this.propertyName = propertyName;
    }

    public Class<?> getHostClass() {
        return hostClass;
    }

    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (hostClass == null ? 0 : hostClass.hashCode());
        return prime * result + (propertyName == null ? 0 : propertyName.hashCode());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final HostClassAndPropertyDouble other = (HostClassAndPropertyDouble) obj;
        if (!Objects.equals(hostClass, other.hostClass) || !Objects.equals(propertyName, other.propertyName)) {
            return false;
        }
        return true;
    }

}
