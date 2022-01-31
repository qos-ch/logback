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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.qos.logback.core.model.Model;

public class DependencyDefinition {

    
    Model dependee;
    List<String> dependenciesList;
    
    public DependencyDefinition(Model dependee, String... dependencies) {
        this.dependee = dependee;
        this.dependenciesList = new ArrayList<>(1);
        
        if(dependencies != null && dependencies.length > 0) {
            dependenciesList.addAll(Arrays.asList(dependencies));
        }
        
    }

    void addDependency(String dependency) {
        this.dependenciesList.add(dependency);
    }
    
    public List<String> getUnmodifiableDependenciesList() {
        return Collections.unmodifiableList(this.dependenciesList);
    }

    public Model getDependee() {
        return dependee;
    }
    
    
    
}
