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
package ch.qos.logback.access.model;

import java.util.Objects;

import ch.qos.logback.core.model.Model;

public class ConfigurationModel extends Model {
    
    private static final long serialVersionUID = 5447825021342728679L;

    public static final String INTERNAL_DEBUG_ATTR = "debug";

    String debug;

    @Override
    protected ConfigurationModel makeNewInstance() {
        return new ConfigurationModel();
    }
    
    @Override
    protected void mirror(Model that) {
        ConfigurationModel actual = (ConfigurationModel) that;
        super.mirror(actual);
        this.debug = actual.debug;
    }
    
    
    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(debug);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConfigurationModel other = (ConfigurationModel) obj;
        return Objects.equals(debug, other.debug);
    }

}
