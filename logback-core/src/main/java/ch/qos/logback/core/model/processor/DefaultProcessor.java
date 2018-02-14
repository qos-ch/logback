package ch.qos.logback.core.model.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.ContextAwareBase;

public class DefaultProcessor extends ContextAwareBase {

    final Context context;
    final InterpretationContext interpretationContext;
    final HashMap<Class<? extends Model>, Class<? extends ModelHandlerBase>> modelClassToHandlerMap = new HashMap<>();

    public DefaultProcessor(Context context, InterpretationContext interpretationContext) {
        this.context = context;
        this.interpretationContext = interpretationContext;
    }

    public void addHandler(Class<? extends Model> modelClass, Class<? extends ModelHandlerBase> handlerClass) {
        modelClassToHandlerMap.put(modelClass, handlerClass);
    }

    public void process() {
        if (interpretationContext.isObjectStackEmpty()) {
            addError("Expecting a Model instance at the top of hte interpretationContext");
            return;
        }
        Object o = interpretationContext.peekObject();

        if (!(o instanceof Model)) {
            addError("Expecting a Model instance at the top of hte interpretationContext");
            return;
        }

        final Model topLevelModel = (Model) o;

        traverse(topLevelModel);
        
    }
   
    void traverse(Model parentModel) {
        for (Model model : parentModel.getSubModels()) {
            traverse(model);
        }
        invokeHandlerForModel(parentModel);
    }
    

    void invokeHandlerForModel(Model model) {
        Class<? extends Model> modelClass = model.getClass();

        Class<? extends ModelHandlerBase> handlerClass = modelClassToHandlerMap.get(modelClass);
        if (handlerClass == null) {
            addError("Can't handle model of type " + modelClass.getName());
            return;
        }

        ModelHandlerBase handler = instantiateHandler(handlerClass);
        if (handler != null)
            handler.handle(interpretationContext, model);
    }
    
    private ModelHandlerBase instantiateHandler(Class<? extends ModelHandlerBase> handlerClass) {

        // =
        try {
            Constructor<? extends ModelHandlerBase> constructor = handlerClass.getConstructor(Context.class);
            return constructor.newInstance(context);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            addError("Failed to construct handler of type ["+handlerClass+"]", e);
        }

        return null;
    }

}
