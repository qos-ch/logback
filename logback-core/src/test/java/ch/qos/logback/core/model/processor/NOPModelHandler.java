package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;

public class NOPModelHandler extends ModelHandlerBase {

    public NOPModelHandler(final Context context) {
        super(context);
    }

    static public NOPModelHandler makeInstance(final Context context, final InterpretationContext ic) {
        return new NOPModelHandler(context);
    }

    @Override
    public void handle(final InterpretationContext interpretationContext, final Model model) {
    }

}
