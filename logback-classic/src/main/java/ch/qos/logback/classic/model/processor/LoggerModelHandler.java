package ch.qos.logback.classic.model.processor;

import static ch.qos.logback.core.joran.JoranConstants.NULL;

import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.model.LoggerModel;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.OptionHelper;

public class LoggerModelHandler extends ModelHandlerBase {

	Logger logger;
	boolean inError = false;

	public LoggerModelHandler(Context context) {
		super(context);
	}

	protected Class<LoggerModel> getSupportedModelClass() {
		return LoggerModel.class;
	}

	@Override
	public void handle(InterpretationContext intercon, Model model) throws ModelHandlerException {
		inError = false;

		LoggerModel loggerModel = (LoggerModel) model;

		String finalLoggerName = intercon.subst(loggerModel.getName());

		LoggerContext loggerContext = (LoggerContext) this.context;
		
		logger = loggerContext.getLogger(finalLoggerName);

		String levelStr = intercon.subst(loggerModel.getLevel());
		if (!OptionHelper.isNullOrEmpty(levelStr)) {
			if (JoranConstants.INHERITED.equalsIgnoreCase(levelStr) || NULL.equalsIgnoreCase(levelStr)) {
				addInfo("Setting level of logger [" + finalLoggerName + "] to null, i.e. INHERITED");
				logger.setLevel(null);
			} else {
				Level level = Level.toLevel(levelStr);
				addInfo("Setting level of logger [" + finalLoggerName + "] to " + level);
				logger.setLevel(level);
			}
		}

		String additivityStr = intercon.subst(loggerModel.getAdditivity());
		if (!OptionHelper.isNullOrEmpty(additivityStr)) {
			boolean additive = OptionHelper.toBoolean(additivityStr, true);
			addInfo("Setting additivity of logger [" + finalLoggerName + "] to " + additive);
			logger.setAdditive(additive);
		}

		attachRefencedAppenders(intercon, loggerModel, logger);
		
		intercon.pushObject(logger);
	}

	static void attachRefencedAppenders(InterpretationContext interpContext, Model model, Logger logger) {
		
		List<String> dependencies = interpContext.getDependencies(model);
		if(dependencies == null || dependencies.isEmpty())
			return;
		
		@SuppressWarnings("unchecked")
		Map<String, Appender<ILoggingEvent>> appenderBag = (Map<String, Appender<ILoggingEvent>>) interpContext.getObjectMap()
				.get(JoranConstants.APPENDER_BAG);
    	
		for(String name: dependencies) {
			Appender<ILoggingEvent> appender = appenderBag.get(name);
			if(appender == null) {
				interpContext.addError("Failed to find appender named ["+name+"]");
			} else {
				interpContext.addInfo("Attaching appender named ["+name+"] to logger ["+logger.getName());
				logger.addAppender(appender);
			}
		}
		
	}

	@Override
	public void postHandle(InterpretationContext intercon, Model model) {
		if (inError) {
			return;
		}
		Object o = intercon.peekObject();
		if (o != logger) {
			LoggerModel loggerModel = (LoggerModel) model;
			addWarn("The object [" + o + "] on the top the of the stack is not the expected logger named "
					+ loggerModel.getName());
		} else {
			intercon.popObject();
		}

	}

}
