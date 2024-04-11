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

public class DefineModel extends NamedComponentModel {

    private static final long serialVersionUID = 6209642548924431065L;
    String scopeStr;

    @Override
    protected DefineModel makeNewInstance() {
        return new DefineModel();
    }
    
    @Override
    protected void mirror(Model that) {
        DefineModel actual = (DefineModel) that;
        super.mirror(actual);
        this.scopeStr = actual.scopeStr;
    }

    public String getScopeStr() {
        return scopeStr;
    }

    public void setScopeStr(String scopeStr) {
        this.scopeStr = scopeStr;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(scopeStr);
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
        DefineModel other = (DefineModel) obj;
        return Objects.equals(scopeStr, other.scopeStr);
    }

    
}
