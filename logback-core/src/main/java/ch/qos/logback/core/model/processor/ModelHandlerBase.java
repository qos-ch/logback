package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.ContextAwareBase;

abstract class ModelHandlerBase extends ContextAwareBase  {

    
    ModelHandlerBase(Context context) {
        setContext(context);
    }
    
    
    abstract void handle(InterpretationContext interpretationContext, Model model);


}
