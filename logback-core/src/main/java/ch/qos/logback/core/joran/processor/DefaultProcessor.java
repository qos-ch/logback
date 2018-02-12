package ch.qos.logback.core.joran.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.hook.DefaultShutdownHook;
import ch.qos.logback.core.hook.ShutdownHookBase;
import ch.qos.logback.core.joran.model.Model;
import ch.qos.logback.core.joran.model.ShutdownHookModel;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.DynamicClassLoadingException;
import ch.qos.logback.core.util.IncompatibleClassException;
import ch.qos.logback.core.util.OptionHelper;

public class DefaultProcessor extends ContextAwareBase {

    final Model topLevelModel;
    Context context;

    public DefaultProcessor(Context context, Model topLevelModel) {
        this.context = context;
        this.topLevelModel = topLevelModel;
    }

    public void process() {
        for (Model model : topLevelModel.getSubModels()) {
            if (model instanceof ShutdownHookModel) {
                handleShudownHookModel((ShutdownHookModel) model);
            }
        }
    }

    private void handleShudownHookModel(ShutdownHookModel model) {
        
        String className = model.getClassName();
        if (OptionHelper.isEmpty(className)) {
            className = DefaultShutdownHook.class.getName();
            addInfo("Assuming className [" + className + "]");
        }
        

        
        addInfo("About to instantiate shutdown hook of type [" + className + "]");
        ShutdownHookBase hook = null;
        try {
            hook = (ShutdownHookBase) OptionHelper.instantiateByClassName(className, ShutdownHookBase.class, context);
            hook.setContext(context);
        } catch (IncompatibleClassException | DynamicClassLoadingException e) {
            addError("Could not create a shutdown hook of type [" + className + "].", e);
        }

        if(hook == null)
            return;
        
        Thread hookThread = new Thread(hook, "Logback shutdown hook [" + context.getName() + "]");
        addInfo("Registeting shuthown hook with JVM runtime.");
        context.putObject(CoreConstants.SHUTDOWN_HOOK_THREAD, hookThread);
        Runtime.getRuntime().addShutdownHook(hookThread);

    }

}
