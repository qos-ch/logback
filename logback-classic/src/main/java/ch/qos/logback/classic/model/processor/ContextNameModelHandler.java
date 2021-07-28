package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.model.ContextNameModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;

public class ContextNameModelHandler extends ModelHandlerBase {

	public ContextNameModelHandler(Context context) {
		super(context);
	}

	static public ModelHandlerBase makeInstance(Context context, InterpretationContext ic) {
		return new ContextNameModelHandler(context);
	}	
		
	
	@Override
	protected Class<ContextNameModel> getSupportedModelClass() {
		return ContextNameModel.class;
	}

	@Override
	public void handle(InterpretationContext intercon, Model model) throws ModelHandlerException {
		ContextNameModel contextNameModel = (ContextNameModel) model;
		
		
		String finalBody = intercon.subst(contextNameModel.getBodyText());
        addInfo("Setting logger context name as [" + finalBody + "]");
        try {
            context.setName(finalBody);
        } catch (IllegalStateException e) {
            addError("Failed to rename context [" + context.getName() + "] as [" + finalBody + "]", e);
        }

	}

}
