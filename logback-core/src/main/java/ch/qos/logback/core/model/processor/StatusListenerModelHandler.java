package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.StatusListenerModel;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.util.OptionHelper;

public class StatusListenerModelHandler extends ModelHandlerBase {


    boolean inError = false;
    Boolean effectivelyAdded = null;
    StatusListener statusListener = null;

    public StatusListenerModelHandler(final Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(final Context context, final InterpretationContext ic) {
        return new StatusListenerModelHandler(context);
    }

    @Override
    protected Class<StatusListenerModel> getSupportedModelClass() {
        return StatusListenerModel.class;
    }

    @Override
    public void handle(final InterpretationContext ic, final Model model) throws ModelHandlerException {

        final StatusListenerModel slModel = (StatusListenerModel) model;

        try {
            statusListener = (StatusListener) OptionHelper.instantiateByClassName(slModel.getClassName(), StatusListener.class, context);
            effectivelyAdded = ic.getContext().getStatusManager().add(statusListener);
            if (statusListener instanceof ContextAware) {
                ((ContextAware) statusListener).setContext(context);
            }
            addInfo("Added status listener of type [" + slModel.getClassName() + "]");
            ic.pushObject(statusListener);
        } catch (final Exception e) {
            inError = true;
            addError("Could not create an StatusListener of type [" + slModel.getClassName() + "].", e);
            throw new ModelHandlerException(e);
        }
    }

    @Override
    public void postHandle(final InterpretationContext ic, final Model m) {
        if (inError) {
            return;
        }

        if (isEffectivelyAdded() && statusListener instanceof LifeCycle) {
            ((LifeCycle) statusListener).start();
        }
        final Object o = ic.peekObject();
        if (o != statusListener) {
            addWarn("The object at the of the stack is not the statusListener pushed earlier.");
        } else {
            ic.popObject();
        }
    }

    private boolean isEffectivelyAdded() {
        if (effectivelyAdded == null) {
            return false;
        }
        return effectivelyAdded;
    }
}
