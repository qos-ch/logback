package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
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

    public StatusListenerModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new StatusListenerModelHandler(context);
    }

    @Override
    protected Class<StatusListenerModel> getSupportedModelClass() {
        return StatusListenerModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext ic, Model model) throws ModelHandlerException {

        StatusListenerModel slModel = (StatusListenerModel) model;

        String className = slModel.getClassName();

        if (OptionHelper.isNullOrEmpty(className)) {
            addError("Empty class name for StatusListener");
            inError = true;
            return;
        } else {
            className = ic.getImport(className);
        }

        try {
            statusListener = (StatusListener) OptionHelper.instantiateByClassName(className, StatusListener.class,
                    context);
            effectivelyAdded = ic.getContext().getStatusManager().add(statusListener);
            if (statusListener instanceof ContextAware) {
                ((ContextAware) statusListener).setContext(context);
            }
            addInfo("Added status listener of type [" + slModel.getClassName() + "]");
            ic.pushObject(statusListener);
        } catch (Exception e) {
            inError = true;
            addError("Could not create an StatusListener of type [" + slModel.getClassName() + "].", e);
            throw new ModelHandlerException(e);
        }
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model m) {
        if (inError) {
            return;
        }

        if (isEffectivelyAdded() && statusListener instanceof LifeCycle) {
            ((LifeCycle) statusListener).start();
        }
        Object o = mic.peekObject();
        if (o != statusListener) {
            addWarn("The object at the of the stack is not the statusListener pushed earlier.");
        } else {
            mic.popObject();
        }
    }

    private boolean isEffectivelyAdded() {
        if (effectivelyAdded == null)
            return false;
        return effectivelyAdded;
    }
}
