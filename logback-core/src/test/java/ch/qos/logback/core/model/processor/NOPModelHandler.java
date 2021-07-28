package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;

public class NOPModelHandler extends ModelHandlerBase {

    public NOPModelHandler(Context context) {
        super(context);
    }

	static public NOPModelHandler makeInstance(Context context, InterpretationContext ic) {
		return new NOPModelHandler(context);
	}
	
    @Override
    public void handle(InterpretationContext interpretationContext, Model model) {
    }

}
