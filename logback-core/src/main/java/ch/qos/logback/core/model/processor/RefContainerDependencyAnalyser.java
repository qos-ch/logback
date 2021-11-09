package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;

public class RefContainerDependencyAnalyser extends ModelHandlerBase {

	final Class<?> modelClass;

	public RefContainerDependencyAnalyser(final Context context, final Class<?> modelClass) {
		super(context);
		this.modelClass = modelClass;
	}

	@Override
	protected boolean isSupportedModelType(final Model model) {

		if (modelClass.isInstance(model)) {
			return true;
		}

		final StringBuilder buf = new StringBuilder("This handler can only handle models of type ");
		buf.append(modelClass.getName());
		addError(buf.toString());
		return false;
	}

	@Override
	public void handle(final InterpretationContext intercon, final Model model) throws ModelHandlerException {
		intercon.pushModel(model);
	}

	@Override
	public void postHandle(final InterpretationContext intercon, final Model model) throws ModelHandlerException {
		final Model poppedModel = intercon.popModel();
		if (model != poppedModel) {
			addError("Popped model [" + poppedModel + "] different than expected [" + model + "]");
		}
	}
}
