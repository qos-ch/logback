package ch.qos.logback.core.model.processor;

import java.util.HashMap;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.ContextAwareBase;

public class DefaultProcessor extends ContextAwareBase {

    final InterpretationContext interpretationContext;
    final HashMap<Class<? extends Model>, ModelHandlerBase> modelClassToHandlerMap = new HashMap<>();

    public DefaultProcessor(Context context, InterpretationContext interpretationContext) {
        this.setContext(context);
        this.interpretationContext = interpretationContext;
    }

    public void addHandler(Class<? extends Model> modelClass, ModelHandlerBase handler) {
        modelClassToHandlerMap.put(modelClass, handler);
    }

    public void process() {
        if (interpretationContext.isModelStackEmpty()) {
            addError("Expecting a Model instance at the top of hte interpretationContext");
            return;
        }

        final Model topLevelModel = interpretationContext.peekModel();
        traverse(topLevelModel);

    }

    void traverse(Model model) {

        ModelHandlerBase handler = modelClassToHandlerMap.get(model.getClass());

        if (handler == null) {
            addError("Can't handle model of type " + model.getClassName() + "  with tag: " + model.getTag());
            return;
        }

        try {
            handler.handle(interpretationContext, model);
            for (Model m : model.getSubModels()) {
                traverse(m);
            }
            handler.postHandle(interpretationContext, model);
        } catch (ModelHandlerException e) {
            addError("Failed to traverse model "+model.getTag(), e);
        }
    }

}
