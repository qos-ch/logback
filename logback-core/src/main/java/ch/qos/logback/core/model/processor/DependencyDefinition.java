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
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.model.Model;

/**
 * Defines the relation between a dependee (Model) and a dependency (String).
 * 
 * Note that a dependee may have multiple dependencies but 
 * {@link DependencyDefinition} applies to just one dependency relation.
 * 
 * @author ceki
 *
 */
public class DependencyDefinition {

    // depender: a component of type Model which depends on a dependee
    Model depender;
    // dependee: the string name of a component depended upon by the depender of type Model
    String dependee;
    
    public DependencyDefinition(Model depender, String dependee) {
        this.depender = depender;
        this.dependee = dependee;
        
        
    }
    
    public String getDependee() {
        return dependee;
    }

    public Model getDepender() {
        return depender;
    }
    
    
    
}
