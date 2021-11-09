package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.model.ContextNameModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;

public class ContextNameModelHandler extends ModelHandlerBase {

	public ContextNameModelHandler(final Context context) {
		super(context);
	}

	static public ModelHandlerBase makeInstance(final Context context, final InterpretationContext ic) {
		return new ContextNameModelHandler(context);
	}


	@Override
	protected Class<ContextNameModel> getSupportedModelClass() {
		return ContextNameModel.class;
	}

	@Override
	public void handle(final InterpretationContext intercon, final Model model) throws ModelHandlerException {
		final ContextNameModel contextNameModel = (ContextNameModel) model;


		final String finalBody = intercon.subst(contextNameModel.getBodyText());
		addInfo("Setting logger context name as [" + finalBody + "]");
		try {
			context.setName(finalBody);
		} catch (final IllegalStateException e) {
			addError("Failed to rename context [" + context.getName() + "] as [" + finalBody + "]", e);
		}

	}

}
