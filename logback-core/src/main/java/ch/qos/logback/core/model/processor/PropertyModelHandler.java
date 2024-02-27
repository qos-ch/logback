package ch.qos.logback.core.model.processor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.joran.action.ActionUtil.Scope;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ModelConstants;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.model.util.PropertyModelHandlerHelper;
import ch.qos.logback.core.util.Loader;

public class PropertyModelHandler extends ModelHandlerBase {

    public PropertyModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new PropertyModelHandler(context);
    }

    @Override
    protected Class<PropertyModel> getSupportedModelClass() {
        return PropertyModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) {

        PropertyModel propertyModel = (PropertyModel) model;
        PropertyModelHandlerHelper propertyModelHandlerHelper = new PropertyModelHandlerHelper(this);
        propertyModelHandlerHelper.setContext(context);
        propertyModelHandlerHelper.handlePropertyModel(mic, propertyModel);
    }

}
