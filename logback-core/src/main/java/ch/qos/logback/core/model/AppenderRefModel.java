/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2021, QOS.ch. All rights reserved.
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

import ch.qos.logback.core.model.processor.PhaseIndicator;
import ch.qos.logback.core.model.processor.ProcessingPhase;

@PhaseIndicator(phase = ProcessingPhase.SECOND)
public class AppenderRefModel extends Model {

    private static final long serialVersionUID = 5238705468395447547L;

    String ref;

    protected AppenderRefModel makeNewInstance() {
        return new AppenderRefModel();
    }
    
    @Override
    protected void mirror(Model that) {
        AppenderRefModel actual = (AppenderRefModel) that;
        super.mirror(actual);
        this.ref = actual.ref;
    }
    
    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(ref);
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
        AppenderRefModel other = (AppenderRefModel) obj;
        return Objects.equals(ref, other.ref);
    }
    


}
