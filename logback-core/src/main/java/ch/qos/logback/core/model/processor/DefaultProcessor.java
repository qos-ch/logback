package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.hook.DefaultShutdownHook;
import ch.qos.logback.core.hook.ShutdownHookBase;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.PropertyModel;
import ch.qos.logback.core.model.ShutdownHookModel;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.DynamicClassLoadingException;
import ch.qos.logback.core.util.IncompatibleClassException;
import ch.qos.logback.core.util.OptionHelper;

public class DefaultProcessor extends ContextAwareBase {

    final Context context;
    final  InterpretationContext interpretationContext;
    final Model topLevelModel;
    

    public DefaultProcessor(Context context, InterpretationContext interpretationContext, Model topLevelModel) {
        this.context = context;
        this.interpretationContext = interpretationContext;
        this.topLevelModel = topLevelModel;
    }

    public void process() {
        for (Model model : topLevelModel.getSubModels()) {
            if (model instanceof ShutdownHookModel) {
                ShutdownHookModelHandler shutdownHookModelHandler = new ShutdownHookModelHandler(context, interpretationContext); 
                shutdownHookModelHandler.handle((ShutdownHookModel) model);
            } else if(model instanceof PropertyModel) {
                PropertyModelHandler propertyModelHandler = new PropertyModelHandler(context, interpretationContext); 
                propertyModelHandler.handle((PropertyModel) model);
            }
        }
    }

   

}
