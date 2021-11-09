package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;

public class AppenderAction extends BaseModelAction {

	@Override
	protected boolean validPreconditions(final InterpretationContext ic, final String name, final Attributes attributes) {
		final PreconditionValidator validator = new PreconditionValidator(this, ic, name, attributes);
		validator.validateClassAttribute();
		validator.validateNameAttribute();
		return validator.isValid();
	}

	@Override
	protected Model buildCurrentModel(final InterpretationContext interpretationContext, final String name, final Attributes attributes) {
		final AppenderModel appenderModel = new AppenderModel();
		appenderModel.setClassName(attributes.getValue(CLASS_ATTRIBUTE));
		appenderModel.setName(attributes.getValue(NAME_ATTRIBUTE));
		return appenderModel;
	}

}
