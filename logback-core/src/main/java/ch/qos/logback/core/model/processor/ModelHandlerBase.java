package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.ContextAwareBase;

abstract public class ModelHandlerBase extends ContextAwareBase {

	public ModelHandlerBase(Context context) {
		setContext(context);
	}

	/**
	 * Subclasses should return the sub-class of Model that they expect to handle.
	 * 
	 * The default implementation assumes that all Model classes are supported. This
	 * a very lax assumption which is usually not true.
	 * 
	 * @return supported model class
	 * @see ModelHandlerBase#isSupportedModelType(Model)
	 */
	protected Class<? extends Model> getSupportedModelClass() {
		// Assume lax default where all model objects are supported
		return Model.class;
	}

	protected boolean isSupportedModelType(Model model) {
		Class<? extends Model> modelClass = getSupportedModelClass();
		if (modelClass.isInstance(model)) {
			return true;
		} else {
			addError("This handler can only handle models of type [" + modelClass + "]");
			return false;
		}
	}

	abstract public void handle(InterpretationContext intercon, Model model) throws ModelHandlerException;

	public void postHandle(InterpretationContext intercon, Model model) throws ModelHandlerException {
		// let specialized handlers override
	}

	public String toString() {
		return this.getClass().getName();
	}

}
