package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.hook.DefaultShutdownHook;
import ch.qos.logback.core.hook.ShutdownHookBase;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ShutdownHookModel;
import ch.qos.logback.core.util.DynamicClassLoadingException;
import ch.qos.logback.core.util.IncompatibleClassException;
import ch.qos.logback.core.util.OptionHelper;

public class ShutdownHookModelHandler extends ModelHandlerBase {

    public ShutdownHookModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, InterpretationContext ic) {
		return new ShutdownHookModelHandler(context);
	}	
	
    
    @Override
    protected Class<ShutdownHookModel> getSupportedModelClass() {
    	return ShutdownHookModel.class;
    }
    
    @Override
    public void handle(InterpretationContext interpretationContext, Model model) {

    	ShutdownHookModel shutdownHookModel = (ShutdownHookModel) model;

        
        String className = shutdownHookModel.getClassName();
        if (OptionHelper.isNullOrEmpty(className)) {
            className = DefaultShutdownHook.class.getName();
            addInfo("Assuming className [" + className + "]");
        } else {
        	className = interpretationContext.getImport(className);
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