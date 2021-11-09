package ch.qos.logback.classic.model.processor;

import static ch.qos.logback.core.joran.JoranConstants.INHERITED;
import static ch.qos.logback.core.joran.JoranConstants.NULL;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.model.LevelModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;

public class LevelModelHandler extends ModelHandlerBase {

    boolean inError = false;

    public LevelModelHandler(final Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(final Context context, final InterpretationContext ic) {
        return new LevelModelHandler(context);
    }

    @Override
    protected Class<? extends LevelModel> getSupportedModelClass() {
        return LevelModel.class;
    }

    @Override
    public void handle(final InterpretationContext intercon, final Model model) throws ModelHandlerException {

        final Object o = intercon.peekObject();

        if (!(o instanceof Logger)) {
            inError = true;
            addError("For element <level>, could not find a logger at the top of execution stack.");
            return;
        }

        final Logger l = (Logger) o;
        final String loggerName = l.getName();

        final LevelModel levelModel = (LevelModel) model;
        final String levelStr = intercon.subst(levelModel.getValue());
        if (INHERITED.equalsIgnoreCase(levelStr) || NULL.equalsIgnoreCase(levelStr)) {
            l.setLevel(null);
        } else {
            l.setLevel(Level.toLevel(levelStr, Level.DEBUG));
        }

        addInfo(loggerName + " level set to " + l.getLevel());

    }

}
