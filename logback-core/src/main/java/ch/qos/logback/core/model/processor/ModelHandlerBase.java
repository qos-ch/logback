package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.ContextAwareBase;

abstract class ModelHandlerBase<M extends Model> extends ContextAwareBase  {

    InterpretationContext interpretationContext;
    
    ModelHandlerBase(Context context,  InterpretationContext interpretationContext) {
        this.interpretationContext = interpretationContext;
        setContext(context);
    }
    
    
    abstract void handle(M model);


}
