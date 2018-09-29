package ch.qos.logback.core.model.processor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

    public void process(Model model) {
        if (model == null) {
            addError("Expecting non null model to process");
            return;
        }

        traverse(model);
    }
    

    void traverse(Model model) {

        ModelHandlerBase handler = modelClassToHandlerMap.get(model.getClass());

        if (handler == null) {
            addError("Can't handle model of type " + model.getClass() + "  with tag: " + model.getTag());
            return;
        }

        System.out.println(model.getClass() + " --> "+handler.getClass());
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
