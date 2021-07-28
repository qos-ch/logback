package ch.qos.logback.core.model;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.processor.ModelHandlerBase;

public interface ModelFactoryMethod {

	public ModelHandlerBase make(Context context, InterpretationContext ic);
}
