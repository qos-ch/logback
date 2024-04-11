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
package ch.qos.logback.core.model;

import java.util.Objects;

public class NamedModel extends Model implements INamedModel {

    private static final long serialVersionUID = 3549881638769570183L;

    String name;

    @Override
    protected NamedModel makeNewInstance() {
        return new NamedModel();
    }
    
    @Override
    protected void mirror(Model that) {
        NamedModel actual = (NamedModel) that;
        super.mirror(actual);
        this.name = actual.name;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(name);
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
        NamedModel other = (NamedModel) obj;
        return Objects.equals(name, other.name);
    }
    
    
}
