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
package ch.qos.logback.classic.model;

import java.util.Objects;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.PhaseIndicator;
import ch.qos.logback.core.model.processor.ProcessingPhase;

@PhaseIndicator(phase = ProcessingPhase.SECOND)
public class RootLoggerModel extends Model {

    private static final long serialVersionUID = -2811453129653502831L;
    String level;

    @Override
    protected RootLoggerModel makeNewInstance() {
        return new RootLoggerModel();
    }
    
    @Override
    protected void mirror(Model that) {
        RootLoggerModel actual = (RootLoggerModel) that;
        super.mirror(actual);
        this.level = actual.level;
    }
    
    
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(level);
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
        RootLoggerModel other = (RootLoggerModel) obj;
        return Objects.equals(level, other.level);
    }
    
    
}
