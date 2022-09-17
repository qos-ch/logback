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
package ch.qos.logback.core.joran.sanity;

import ch.qos.logback.core.model.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for sanity checking Models.
 * @since 1.3.2/1.4.2
 * @author ceki
 */
public interface SanityChecker {

    public void check(Model model);

    default void deepFindAllModelsOfType(Class<? extends Model> modelClass, List<Model> modelList, Model model) {
        if (modelClass.isInstance(model)) {
            modelList.add(model);
        }

        for (Model m : model.getSubModels()) {
            deepFindAllModelsOfType(modelClass, modelList, m);
        }
    }

    default List<Pair<Model, Model>> deepFindNestedSubModelsOfType(Class<? extends Model> modelClass, List<? extends Model> parentList) {

        List<Pair<Model, Model>> nestingPairs = new ArrayList<>();

        for (Model parent : parentList) {
            List<Model> nestedElements = new ArrayList<>();
            parent.getSubModels().stream().forEach(m -> deepFindAllModelsOfType(modelClass, nestedElements, m));
            nestedElements.forEach(n -> nestingPairs.add(new Pair(parent, n)));
        }
        return nestingPairs;
    }
}
