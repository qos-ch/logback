/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2002, QOS.ch. All rights reserved.
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

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.ImcplicitActionDataForBasicProperty;
import ch.qos.logback.core.joran.action.ImplicitModelData;
import ch.qos.logback.core.joran.action.ImplicitModelDataForComplexProperty;
import ch.qos.logback.core.joran.spi.NoAutoStartUtil;
import ch.qos.logback.core.joran.util.PropertySetter;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.model.ComponentModel;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ModelConstants;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.AggregationType;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

public class ImplicitModelHandler extends ModelHandlerBase {

    private final BeanDescriptionCache beanDescriptionCache;
    private ImplicitModelData implicitModelData;

    static public final String IGNORING_UNKNOWN_PROP = "Ignoring unknown property";

    boolean inError = false;

    public ImplicitModelHandler(Context context, BeanDescriptionCache beanDescriptionCache) {
        super(context);
        this.beanDescriptionCache = beanDescriptionCache;
    }

    protected Class<? extends ImplicitModel> getSupportedModelClass() {
        return ImplicitModel.class;
    }

    static public ImplicitModelHandler makeInstance(Context context, ModelInterpretationContext mic) {
        BeanDescriptionCache beanDescriptionCache = mic.getBeanDescriptionCache();
        return new ImplicitModelHandler(context, beanDescriptionCache);
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) {

        ImplicitModel implicitModel = (ImplicitModel) model;

        // calling intercon.peekObject with an empty stack will throw an exception
        if (mic.isObjectStackEmpty()) {
            inError = true;
            return;
        }
        String nestedElementTagName = implicitModel.getTag();

        Object o = mic.peekObject();
        PropertySetter parentBean = new PropertySetter(beanDescriptionCache, o);
        parentBean.setContext(context);

        AggregationType aggregationType = parentBean.computeAggregationType(nestedElementTagName);

        switch (aggregationType) {
        case NOT_FOUND:
            addWarn(IGNORING_UNKNOWN_PROP+" [" + nestedElementTagName + "] in [" + o.getClass().getName() + "]");
            this.inError = true;
            // no point in processing submodels
            implicitModel.markAsSkipped();
            return;
        case AS_BASIC_PROPERTY:
        case AS_BASIC_PROPERTY_COLLECTION:
            ImcplicitActionDataForBasicProperty adBasicProperty = new ImcplicitActionDataForBasicProperty(parentBean,
                    aggregationType, nestedElementTagName);
            this.implicitModelData = adBasicProperty;
            doBasicProperty(mic, implicitModel, adBasicProperty);
            return;
        // we only push action data if NestComponentIA is applicable
        case AS_COMPLEX_PROPERTY_COLLECTION:
        case AS_COMPLEX_PROPERTY:
            Class<?> propertyType = parentBean.getTypeForComplexProperty(nestedElementTagName, aggregationType);
            ImplicitModelDataForComplexProperty imdForComplexProperty = new ImplicitModelDataForComplexProperty(parentBean,
                    aggregationType, nestedElementTagName);
            imdForComplexProperty.setExpectedPropertyType(propertyType);
            this.implicitModelData = imdForComplexProperty;
            doComplex(mic, implicitModel, imdForComplexProperty);
            return;
        default:
            addError("PropertySetter.computeAggregationType returned " + aggregationType);
            return;
        }

    }

    void doBasicProperty(ModelInterpretationContext interpretationContext, Model model,
            ImcplicitActionDataForBasicProperty actionData) {
        String finalBody = interpretationContext.subst(model.getBodyText());
        // get the action data object pushed in isApplicable() method call
        // IADataForBasicProperty actionData = (IADataForBasicProperty)
        // actionDataStack.peek();
        switch (actionData.aggregationType) {
        case AS_BASIC_PROPERTY:
            actionData.parentBean.setProperty(actionData.propertyName, finalBody);
            break;
        case AS_BASIC_PROPERTY_COLLECTION:
            actionData.parentBean.addBasicProperty(actionData.propertyName, finalBody);
            break;
        default:
            addError("Unexpected aggregationType " + actionData.aggregationType);
        }
    }

    public void doComplex(ModelInterpretationContext interpretationContext, ComponentModel componentModel,
            ImplicitModelDataForComplexProperty imdForComplexProperty) {

        String propertyClassName = componentModel.getClassName();
        // perform variable name substitution
        String substPropertyClassName = interpretationContext.subst(propertyClassName);

        String fqcn = interpretationContext.getImport(substPropertyClassName);

        Class<?> propertyClass = null;
        try {

            if (!OptionHelper.isNullOrEmptyOrAllSpaces(fqcn)) {
                propertyClass = Loader.loadClass(fqcn, context);
            } else {
                // guess class name via implicit rules
                PropertySetter parentBean = imdForComplexProperty.parentBean;
                propertyClass = parentBean.getClassNameViaImplicitRules(imdForComplexProperty.propertyName,
                        imdForComplexProperty.getAggregationType(), interpretationContext.getDefaultNestedComponentRegistry());
            }

            if (propertyClass == null) {
                imdForComplexProperty.inError = true;
                String errMsg = "Could not find an appropriate class for property [" + componentModel.getTag() + "]";
                addError(errMsg);
                return;
            }

            if (OptionHelper.isNullOrEmptyOrAllSpaces(fqcn)) {
                addInfo("Assuming default type [" + propertyClass.getName() + "] for [" + componentModel.getTag()
                        + "] property");
            }



            Class<?> expectedPropertyType = imdForComplexProperty.getExpectedPropertyType();

            Object object = OptionHelper.instantiateClassWithSuperclassRestriction(propertyClass, expectedPropertyType);

            imdForComplexProperty.setNestedComplexProperty(object);

            // pass along the context
            if (imdForComplexProperty.getNestedComplexProperty() instanceof ContextAware) {
                ((ContextAware) imdForComplexProperty.getNestedComplexProperty()).setContext(this.context);
            }
            // addInfo("Pushing component [" + localName
            // + "] on top of the object stack.");
            interpretationContext.pushObject(imdForComplexProperty.getNestedComplexProperty());

        } catch (Exception oops) {
            imdForComplexProperty.inError = true;
            String msg = "Could not create component [" + componentModel.getTag() + "] of type [" + fqcn + "]";
            addError(msg, oops);
        }
    }

    @Override
    public void postHandle(ModelInterpretationContext intercon, Model model) {
        if (inError) {
            return;
        }

        if(implicitModelData == null)
            return;
        
        // the action data can in an incorrect state, in which case we need to 
        // disengage
        if(implicitModelData.inError) {
            return;
        }
        if (implicitModelData instanceof ImplicitModelDataForComplexProperty) {
            postHandleComplex(intercon, model, (ImplicitModelDataForComplexProperty) implicitModelData);
        }

    }

    private void postHandleComplex(ModelInterpretationContext mic, Model model,
            ImplicitModelDataForComplexProperty imdComplex) {

        PropertySetter nestedBean = new PropertySetter(beanDescriptionCache,
                imdComplex.getNestedComplexProperty());
        nestedBean.setContext(context);

        // have the nested element point to its parent if possible
        if (nestedBean.computeAggregationType(ModelConstants.PARENT_PROPPERTY_KEY) == AggregationType.AS_COMPLEX_PROPERTY) {
            nestedBean.setComplexProperty(ModelConstants.PARENT_PROPPERTY_KEY, imdComplex.parentBean.getObj());
        }

        // start the nested complex property if it implements LifeCycle and is not
        // marked with a @NoAutoStart annotation
        Object nestedComplexProperty = imdComplex.getNestedComplexProperty();
        if (NoAutoStartUtil.shouldBeStarted(nestedComplexProperty)) {
            ((LifeCycle) nestedComplexProperty).start();
        }

        Object o = mic.peekObject();

        if (o != imdComplex.getNestedComplexProperty()) {
            addError("The object on the top the of the stack is not the component pushed earlier.");
        } else {
            mic.popObject();
            // Now let us attach the component
            switch (imdComplex.aggregationType) {
            case AS_COMPLEX_PROPERTY:
                imdComplex.parentBean.setComplexProperty(model.getTag(), imdComplex.getNestedComplexProperty());

                break;
            case AS_COMPLEX_PROPERTY_COLLECTION:
                imdComplex.parentBean.addComplexProperty(model.getTag(), imdComplex.getNestedComplexProperty());
                break;
            default:
                addError("Unexpected aggregationType " + imdComplex.aggregationType);
            }
        }
    }

}
