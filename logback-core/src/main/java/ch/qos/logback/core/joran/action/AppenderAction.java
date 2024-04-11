package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;

public class AppenderAction extends BaseModelAction {

    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext ic, String name, Attributes attributes) {
        PreconditionValidator validator = new PreconditionValidator(this, ic, name, attributes);
        validator.validateClassAttribute();
        validator.validateNameAttribute();
        return validator.isValid();
    }

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name,
            Attributes attributes) {
        AppenderModel appenderModel = new AppenderModel();
        appenderModel.setClassName(attributes.getValue(CLASS_ATTRIBUTE));
        appenderModel.setName(attributes.getValue(NAME_ATTRIBUTE));
        return appenderModel;
    }

}
