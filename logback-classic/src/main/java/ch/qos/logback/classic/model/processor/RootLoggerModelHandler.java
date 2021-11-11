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

    public RootLoggerModelHandler(final Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(final Context context, final InterpretationContext ic) {
        return new RootLoggerModelHandler(context);
    }

    @Override
    protected Class<RootLoggerModel> getSupportedModelClass() {
        return RootLoggerModel.class;
    }

    @Override
    public void handle(final InterpretationContext interpretationContext, final Model model) throws ModelHandlerException {
        inError = false;

        final RootLoggerModel rootLoggerModel = (RootLoggerModel) model;

        final LoggerContext loggerContext = (LoggerContext) context;
        root = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        final String levelStr = interpretationContext.subst(rootLoggerModel.getLevel());
        if (!OptionHelper.isNullOrEmpty(levelStr)) {
            final Level level = Level.toLevel(levelStr);
            addInfo("Setting level of ROOT logger to " + level);
            root.setLevel(level);
        }

        interpretationContext.pushObject(root);
    }

    @Override
    public void postHandle(final InterpretationContext ic, final Model model) {
        if (inError) {
            return;
        }
        final Object o = ic.peekObject();
        if (o != root) {
            addWarn("The object [" + o + "] on the top the of the stack is not the root logger");
        } else {
            ic.popObject();
        }
    }

}
