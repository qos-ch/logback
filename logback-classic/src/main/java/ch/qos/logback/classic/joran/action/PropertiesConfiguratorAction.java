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

package ch.qos.logback.classic.joran.action;

import ch.qos.logback.classic.model.PropertiesConfiguratorModel;
import ch.qos.logback.core.joran.action.ResourceAction;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import org.xml.sax.Attributes;

import static ch.qos.logback.classic.joran.action.ConfigurationAction.SCAN_ATTR;

/**
 * Build an {@link PropertiesConfiguratorModel} instance from SAX events.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.5.8
 */
public class PropertiesConfiguratorAction extends ResourceAction {


    /**
     * Creates a new instance of {@link PropertiesConfiguratorModel}.
     *
     * @return a new {@link PropertiesConfiguratorModel} instance
     */
    protected PropertiesConfiguratorModel makeNewResourceModel() {
        return new PropertiesConfiguratorModel();
    }


    /**
     * Builds a {@link PropertiesConfiguratorModel} instance for the current XML element.
     *
     * <p>This method extends the parent class behavior by additionally extracting and setting
     * the scan attribute value on the returned model.</p>
     *
     * @param saxEventInterpretationContext the context for interpreting SAX events
     * @param localName the name of the XML element being processed
     * @param attributes the attributes of the XML element
     * @return a configured {@link PropertiesConfiguratorModel} instance
     * @throws IllegalStateException if the model returned by the parent class is not a
     *                               {@link PropertiesConfiguratorModel}
     */
    @Override
    public Model buildCurrentModel(SaxEventInterpretationContext saxEventInterpretationContext, String localName,
                                   Attributes attributes) {
        Model model = super.buildCurrentModel(saxEventInterpretationContext, localName, attributes);

        if (model instanceof PropertiesConfiguratorModel) {
            PropertiesConfiguratorModel propertiesConfiguratorModel = (PropertiesConfiguratorModel) model;
            String scanAttribute = attributes.getValue(SCAN_ATTR);
            propertiesConfiguratorModel.setScanStr(scanAttribute);
            return propertiesConfiguratorModel;
        } else {
            // this is impossible since makeNewResourceModel() returns a PropertiesConfiguratorModel
            throw new IllegalStateException("Model is not of type PropertiesConfiguratorModel");
        }
    }
}

