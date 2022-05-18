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
public class LoggerModel extends Model {

    private static final long serialVersionUID = 5326913660697375316L;

    String name;
    String level;
    String additivity;

    @Override
    protected LoggerModel makeNewInstance() {
        return new LoggerModel();
    }
    
    @Override
    protected void mirror(Model that) {
        LoggerModel actual = (LoggerModel) that;
        super.mirror(actual);
        this.name = actual.name;
        this.level = actual.level;
        this.additivity = actual.additivity;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getAdditivity() {
        return additivity;
    }

    public void setAdditivity(String additivity) {
        this.additivity = additivity;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " name=" + name + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(additivity, level, name);
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
        LoggerModel other = (LoggerModel) obj;
        return Objects.equals(additivity, other.additivity) && Objects.equals(level, other.level)
                && Objects.equals(name, other.name);
    }
    
    
}
