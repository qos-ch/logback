package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.model.RootLoggerModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.util.OptionHelper;

public class RootLoggerModelHandler extends ModelHandlerBase {

	Logger root;
	boolean inError = false;

	public RootLoggerModelHandler(Context context) {
		super(context);
	}

	static public ModelHandlerBase makeInstance(Context context, InterpretationContext ic) {
		return new RootLoggerModelHandler(context);
	}	
	
	protected Class<RootLoggerModel> getSupportedModelClass() {
		return RootLoggerModel.class;
	}

	@Override
	public void handle(InterpretationContext interpretationContext, Model model) throws ModelHandlerException {
		inError = false;

		RootLoggerModel rootLoggerModel = (RootLoggerModel) model;

		LoggerContext loggerContext = (LoggerContext) this.context;
		root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

		String levelStr = interpretationContext.subst(rootLoggerModel.getLevel());
		if (!OptionHelper.isNullOrEmpty(levelStr)) {
			Level level = Level.toLevel(levelStr);
			addInfo("Setting level of ROOT logger to " + level);
			root.setLevel(level);
		}
		
		interpretationContext.pushObject(root);
	}

	@Override
	public void postHandle(InterpretationContext ic, Model model) {
		if (inError) {
			return;
		}
		Object o = ic.peekObject();
		if (o != root) {
			addWarn("The object ["+o+"] on the top the of the stack is not the root logger");
		} else {
			ic.popObject();
		}
	}

}
