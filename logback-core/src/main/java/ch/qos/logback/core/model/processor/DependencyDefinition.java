/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.model.Model;

/**
 * Defines the relation between a depender (of type Model) and a dependency name (String).
 * 
 * Note that a depender may have multiple dependencies but
 * {@link DependencyDefinition} applies to just one dependency relation.
 * 
 * @author ceki
 *
 */
public class DependencyDefinition {

    // OLD terminology: dependee (=dependency), dependent(=depender)
    //
    // NEW terminology: *dependent*: a component of type Model which depends on a *dependency*
    Model depender;
    // dependee or dependency: the string name of a component depended upon by the depender of type Model
    String dependency;
    
    public DependencyDefinition(Model depender, String dependency) {
        this.depender = depender;
        this.dependency = dependency;
        
        
    }
    
    public String getDependency() {
        return dependency;
    }

    public Model getDepender() {
        return depender;
    }


    @Override
    public String toString() {
        return "DependencyDefinition{" +
                "depender=" + depender +
                ", dependency='" + dependency + '\'' +
                '}';
    }
}
