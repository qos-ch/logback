package ch.qos.logback.classic.model.processor;

import static ch.qos.logback.core.joran.JoranConstants.INHERITED;
import static ch.qos.logback.core.joran.JoranConstants.NULL;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.model.LevelModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.ErrorCodes;

public class LevelModelHandler extends ModelHandlerBase {

    boolean inError = false;

    public LevelModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new LevelModelHandler(context);
    }

    @Override
    protected Class<? extends LevelModel> getSupportedModelClass() {
        return LevelModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {

        Object o = mic.peekObject();

        if (!(o instanceof Logger)) {
            inError = true;
            addError("For element <level>, could not find a logger at the top of execution stack.");
            return;
        }

        Logger l = (Logger) o;
        String loggerName = l.getName();

        LevelModel levelModel = (LevelModel) model;
        String levelStr = mic.subst(levelModel.getValue());
        if (INHERITED.equalsIgnoreCase(levelStr) || NULL.equalsIgnoreCase(levelStr)) {
            if(Logger.ROOT_LOGGER_NAME.equalsIgnoreCase(loggerName))
                addError(ErrorCodes.ROOT_LEVEL_CANNOT_BE_SET_TO_NULL);
            else
               l.setLevel(null);
        } else {
            l.setLevel(Level.toLevel(levelStr, Level.DEBUG));
        }

        addInfo(loggerName + " level set to " + l.getLevel());

    }

}
