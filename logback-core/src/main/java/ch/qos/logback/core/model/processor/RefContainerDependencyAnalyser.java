package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;

public class RefContainerDependencyAnalyser extends ModelHandlerBase {

	final Class<?> modelClass;

	public RefContainerDependencyAnalyser(Context context, Class<?> modelClass) {
		super(context);
		this.modelClass = modelClass;
	}

	@Override
	protected boolean isSupportedModelType(Model model) {

		if (modelClass.isInstance(model)) {
			return true;
		}

		StringBuilder buf = new StringBuilder("This handler can only handle models of type ");
		buf.append(modelClass.getName());
		addError(buf.toString());
		return false;
	}

	@Override
	public void handle(InterpretationContext intercon, Model model) throws ModelHandlerException {
		intercon.pushModel(model);
	}

	@Override
	public void postHandle(InterpretationContext intercon, Model model) throws ModelHandlerException {
		Model poppedModel = intercon.popModel();
		if (model != poppedModel) {
			addError("Popped model [" + poppedModel + "] different than expected [" + model + "]");
		}
	}
}
