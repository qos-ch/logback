package ch.qos.logback.classic.model.processor;

import static ch.qos.logback.core.joran.JoranConstants.NULL;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.model.LoggerModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.ErrorCodes;
import ch.qos.logback.core.util.OptionHelper;

public class LoggerModelHandler extends ModelHandlerBase {

    Logger logger;
    boolean inError = false;

    public LoggerModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext mic) {
        return new LoggerModelHandler(context);
    }

    protected Class<LoggerModel> getSupportedModelClass() {
        return LoggerModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        inError = false;

        LoggerModel loggerModel = (LoggerModel) model;

        String finalLoggerName = mic.subst(loggerModel.getName());

        LoggerContext loggerContext = (LoggerContext) this.context;

        logger = loggerContext.getLogger(finalLoggerName);

        String levelStr = mic.subst(loggerModel.getLevel());
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(levelStr)) {
            if (JoranConstants.INHERITED.equalsIgnoreCase(levelStr) || NULL.equalsIgnoreCase(levelStr)) {
                if(Logger.ROOT_LOGGER_NAME.equalsIgnoreCase(finalLoggerName)) {
                    addError(ErrorCodes.ROOT_LEVEL_CANNOT_BE_SET_TO_NULL);
                } else {
                    addInfo("Setting level of logger [" + finalLoggerName + "] to null, i.e. INHERITED");
                    logger.setLevel(null);
                }
            } else {
                Level level = Level.toLevel(levelStr);
                addInfo("Setting level of logger [" + finalLoggerName + "] to " + level);
                logger.setLevel(level);
            }
        }

        String additivityStr = mic.subst(loggerModel.getAdditivity());
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(additivityStr)) {
            boolean additive = OptionHelper.toBoolean(additivityStr, true);
            addInfo("Setting additivity of logger [" + finalLoggerName + "] to " + additive);
            logger.setAdditive(additive);
        }

        mic.pushObject(logger);
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) {
        if (inError) {
            return;
        }
        Object o = mic.peekObject();
        if (o != logger) {
            LoggerModel loggerModel = (LoggerModel) model;
            addWarn("The object [" + o + "] on the top the of the stack is not the expected logger named "
                    + loggerModel.getName());
        } else {
            mic.popObject();
        }

    }

}
