package ch.qos.logback.core.joran.implicitAction;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class FruitContextModelHandler extends ModelHandlerBase {

    public FruitContextModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new FruitContextModelHandler(context);
    }

    @Override
    public void handle(ModelInterpretationContext interpretationContext, Model model) throws ModelHandlerException {
        interpretationContext.pushObject(context);
    }

    @Override
    public void postHandle(ModelInterpretationContext ec, Model model) throws ModelHandlerException {

        Object o = ec.peekObject();

        if (o != context) {
            addWarn("The object [" + o + "] at top of the stack is not the context named [" + context.getName()
                    + "] pushed earlier.");
        } else {
            addInfo("Popping context named [" + context.getName() + "] from the object stack");
            ec.popObject();
        }
    }

}
