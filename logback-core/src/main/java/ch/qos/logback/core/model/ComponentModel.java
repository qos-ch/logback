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

/**
 * Abstract representation of configuration elements
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.3.0
 */
public class ComponentModel extends Model {

    private static final long serialVersionUID = -7117814935763453139L;

    String className;

    @Override
    protected ComponentModel makeNewInstance() {
        return new ComponentModel();
    }
    
    @Override
    protected void mirror(Model that) {
        ComponentModel actual = (ComponentModel) that;
        super.mirror(actual);
        this.className = actual.className;
    }
    
    
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [tag=" + tag + ", className=" + className + ", bodyText=" + bodyText
                + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(className);
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
        ComponentModel other = (ComponentModel) obj;
        return Objects.equals(className, other.className);
    }

}