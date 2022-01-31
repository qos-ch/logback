package ch.qos.logback.core.model;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public interface ModelFactoryMethod {

    public ModelHandlerBase make(Context context, ModelInterpretationContext ic);
}
