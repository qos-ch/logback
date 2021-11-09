package ch.qos.logback.classic.model.processor;

import static ch.qos.logback.core.joran.JoranConstants.NULL;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.model.LoggerModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.util.OptionHelper;

public class LoggerModelHandler extends ModelHandlerBase {

    Logger logger;
    boolean inError = false;

    public LoggerModelHandler(final Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(final Context context, final InterpretationContext ic) {
        return new LoggerModelHandler(context);
    }

    @Override
    protected Class<LoggerModel> getSupportedModelClass() {
        return LoggerModel.class;
    }

    @Override
    public void handle(final InterpretationContext intercon, final Model model) throws ModelHandlerException {
        inError = false;

        final LoggerModel loggerModel = (LoggerModel) model;

        final String finalLoggerName = intercon.subst(loggerModel.getName());

        final LoggerContext loggerContext = (LoggerContext) context;

        logger = loggerContext.getLogger(finalLoggerName);

        final String levelStr = intercon.subst(loggerModel.getLevel());
        if (!OptionHelper.isNullOrEmpty(levelStr)) {
            if (JoranConstants.INHERITED.equalsIgnoreCase(levelStr) || NULL.equalsIgnoreCase(levelStr)) {
                addInfo("Setting level of logger [" + finalLoggerName + "] to null, i.e. INHERITED");
                logger.setLevel(null);
            } else {
                final Level level = Level.toLevel(levelStr);
                addInfo("Setting level of logger [" + finalLoggerName + "] to " + level);
                logger.setLevel(level);
            }
        }

        final String additivityStr = intercon.subst(loggerModel.getAdditivity());
        if (!OptionHelper.isNullOrEmpty(additivityStr)) {
            final boolean additive = OptionHelper.toBoolean(additivityStr, true);
            addInfo("Setting additivity of logger [" + finalLoggerName + "] to " + additive);
            logger.setAdditive(additive);
        }

        intercon.pushObject(logger);
    }

    @Override
    public void postHandle(final InterpretationContext intercon, final Model model) {
        if (inError) {
            return;
        }
        final Object o = intercon.peekObject();
        if (o != logger) {
            final LoggerModel loggerModel = (LoggerModel) model;
            addWarn("The object [" + o + "] on the top the of the stack is not the expected logger named "
                            + loggerModel.getName());
        } else {
            intercon.popObject();
        }

    }

}
