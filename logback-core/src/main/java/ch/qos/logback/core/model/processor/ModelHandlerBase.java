package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.ContextAwareBase;

abstract public class ModelHandlerBase extends ContextAwareBase  {

    
    public ModelHandlerBase(Context context) {
        setContext(context);
    }
    
    abstract public void handle(InterpretationContext interpretationContext, Model model) throws ModelHandlerException;

    public void postHandle(InterpretationContext interpretationContext, Model model) throws ModelHandlerException {
        // let specialized handlers override
    }


}
