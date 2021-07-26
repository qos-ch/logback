package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.model.LoggerModel;
import ch.qos.logback.classic.model.RootLoggerModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;

public class LoggerOrAppenderModelDependencyAnalyser extends ModelHandlerBase {

	public LoggerOrAppenderModelDependencyAnalyser(Context context) {
		super(context);
	}
	
	@Override
	protected boolean isSupportedModelType(Model model) {
		
		if (LoggerModel.class.isInstance(model) || RootLoggerModel.class.isInstance(model)
				|| AppenderModel.class.isInstance(model)) {
			return true;
		} else {
			addError("This handler can only handle models of type LoggerModel or RootLoggerModel");
			return false;
		}
	}
	
	@Override
	public void handle(InterpretationContext intercon, Model model) throws ModelHandlerException {
		intercon.pushModel(model);
	}

	public void postHandle(InterpretationContext intercon, Model model) throws ModelHandlerException {
		Model poppedModel = intercon.popModel();
		if(model != poppedModel) {
			addError("Popped model ["+poppedModel + "] different than expected ["+model+"]");
		}
	}
}
