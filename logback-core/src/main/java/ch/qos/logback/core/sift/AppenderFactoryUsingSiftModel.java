/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.sift;

import java.util.Collection;
import java.util.Map;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.ParamModelHandler;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ParamModel;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.model.SiftModel;
import ch.qos.logback.core.model.processor.AppenderModelHandler;
import ch.qos.logback.core.model.processor.ImplicitModelHandler;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.model.processor.PropertyModelHandler;

/**
 * Builds new appenders dynamically by running SiftingJoranConfigurator instance,
 * a custom configurator tailored for the contents of the sift element.
 * @param <E>
 */
public class AppenderFactoryUsingSiftModel<E> implements AppenderFactory<E> {
    
    Context context;
    final Model siftModel;
    protected String discriminatingKey;
    protected ModelInterpretationContext parentMic;
    protected DefaultNestedComponentRegistry registry;
    
    public AppenderFactoryUsingSiftModel(ModelInterpretationContext parentMic, Model aSiftModel, String discriminatingKey)  {
        this.siftModel = Model.duplicate(aSiftModel);
        this.discriminatingKey = discriminatingKey;
        this.parentMic = parentMic;
        this.context = parentMic.getContext();
      
    }


    public SiftProcessor<E> getSiftingModelProcessor(String value) {
        ModelInterpretationContext smic = new ModelInterpretationContext(parentMic) {
            @Override
            public boolean hasDependers(String dependeeName) {
                return true;
            } 
        };
        SiftProcessor<E> siftProcessor = new SiftProcessor<>(context, smic); 
        siftProcessor.addHandler(ParamModel.class, ParamModelHandler::makeInstance);
        siftProcessor.addHandler(PropertyModel.class, PropertyModelHandler::makeInstance);
        siftProcessor.addHandler(ImplicitModel.class, ImplicitModelHandler::makeInstance);
        siftProcessor.addHandler(AppenderModel.class, AppenderModelHandler::makeInstance);
        siftProcessor.addHandler(SiftModel.class, NOPSiftModelHandler::makeInstance);
        
        return siftProcessor;

    }

    public Appender<E> buildAppender(Context context, String discriminatingValue) throws JoranException {
        
        SiftProcessor<E> sp = getSiftingModelProcessor(discriminatingValue);
        ModelInterpretationContext mic = sp.getModelInterpretationContext();
        sp.setContext(context);
        Model duplicate = Model.duplicate(siftModel);
        mic.addSubstitutionProperty(discriminatingKey, discriminatingValue);
        sp.process(duplicate);
        @SuppressWarnings("unchecked")
        Map<String, Appender<E>> appenderBag = (Map<String, Appender<E>>) mic.getObjectMap()
                .get(JoranConstants.APPENDER_BAG);
        Collection<Appender<E>> values = appenderBag.values();
        if (values.size() == 0) {
            return null;
        }
        return values.iterator().next();
    }

    public Model getSiftModel() {
        return siftModel;
    }

}
