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
package ch.qos.logback.classic.model.util;

import java.util.List;

import ch.qos.logback.classic.model.processor.LogbackClassicDefaultNestedComponentRules;
import ch.qos.logback.core.joran.util.ParentTag_Tag_Class_Tuple;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.util.TagUtil;

/**
 * Injects missing class names into ImplicitModel instances missing class name.
 * 
 * @author ceki
 * @since 1.3.0-alpha15
 */
public class DefaultClassNameHelper {

    List<ParentTag_Tag_Class_Tuple> tupleList = LogbackClassicDefaultNestedComponentRules.TUPLES_LIST;

    /**
     * This method injects default components classes to implicit models missing a
     * class name.
     * 
     * @param tupleList
     * @param aModel
     * @param parent
     */
    public void injectDefaultComponentClasses(Model aModel, Model parent) {

        applyInjectionRules(aModel, parent);

        for (Model sub : aModel.getSubModels()) {
            injectDefaultComponentClasses(sub, aModel);
        }
    }

    private void applyInjectionRules(Model aModel, Model parent) {
        if (parent == null)
            return;

        String parentTag = TagUtil.unifiedTag(parent);
        String modelTag = TagUtil.unifiedTag(aModel);

        if (aModel instanceof ImplicitModel) {
            ImplicitModel implicitModel = (ImplicitModel) aModel;
            String className = implicitModel.getClassName();

            if (className == null || className.isEmpty()) {
                for (ParentTag_Tag_Class_Tuple ruleTuple : tupleList) {
                    if (ruleTuple.parentTag.equals(parentTag) && ruleTuple.tag.equals(modelTag)) {
                        implicitModel.setClassName(ruleTuple.className);
                        break;
                    }
                }

            }
        }
    }
}
