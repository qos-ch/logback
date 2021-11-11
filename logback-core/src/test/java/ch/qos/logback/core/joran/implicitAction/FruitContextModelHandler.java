package ch.qos.logback.core.joran.implicitAction;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;

public class FruitContextModelHandler extends ModelHandlerBase {

    public FruitContextModelHandler(final Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(final Context context, final InterpretationContext ic) {
        return new FruitContextModelHandler(context);
    }

    @Override
    public void handle(final InterpretationContext interpretationContext, final Model model) throws ModelHandlerException {
        interpretationContext.pushObject(context);
    }

    @Override
    public void postHandle(final InterpretationContext ec, final Model model) throws ModelHandlerException {

        final Object o = ec.peekObject();

        if (o != context) {
            addWarn("The object [" + o + "] at top of the stack is not the context named [" + context.getName() + "] pushed earlier.");
        } else {
            addInfo("Popping context named [" + context.getName() + "] from the object stack");
            ec.popObject();
        }
    }

}
