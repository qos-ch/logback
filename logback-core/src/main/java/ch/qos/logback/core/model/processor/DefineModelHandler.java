package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.joran.action.ActionUtil.Scope;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.DefineModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.spi.PropertyDefiner;
import ch.qos.logback.core.util.OptionHelper;

/**
 * Instantiate class for define property value. Get future property name and
 * property definer class from attributes. Some property definer properties
 * could be used. After defining put new property to context.
 * 
 * @author Aleksey Didik
 */
public class DefineModelHandler extends ModelHandlerBase {

    boolean inError;
    PropertyDefiner definer;
    String propertyName;
    Scope scope;

    public DefineModelHandler(Context context) {
        super(context);
    }
    
	static public DefineModelHandler makeInstance(Context context, InterpretationContext ic) {
		return new DefineModelHandler(context);
	}
	
    @Override
    protected Class<DefineModel> getSupportedModelClass() {
    	return DefineModel.class;
    }
    
    @Override
    public void handle(InterpretationContext interpretationContext, Model model) throws ModelHandlerException {
        definer = null;
        inError = false;
        propertyName = null;

        
        DefineModel defineModel = (DefineModel) model;

        propertyName = defineModel.getName();
        String scopeStr = defineModel.getScopeStr();

        scope = ActionUtil.stringToScope(scopeStr);

        if (OptionHelper.isNullOrEmpty(propertyName)) {
            addError("Missing property name for property definer. Near [" + model.getTag() + "] line " + model.getLineNumber());
            inError = true;
        }

        // read property definer class name
        String className = defineModel.getClassName();
        if (OptionHelper.isNullOrEmpty(className)) {
            addError("Missing class name for property definer. Near [" + model.getTag() + "] line " + model.getLineNumber());
            inError = true;
        } else {
        	className = interpretationContext.getImport(className);
        }

        if (inError)
            return;

        // try to instantiate property definer
        try {
            addInfo("About to instantiate property definer of type [" + className + "]");
            definer = (PropertyDefiner) OptionHelper.instantiateByClassName(className, PropertyDefiner.class, context);
            definer.setContext(context);
            interpretationContext.pushObject(definer);
        } catch (Exception oops) {
            inError = true;
            addError("Could not create an PropertyDefiner of type [" + className + "].", oops);
            throw new ModelHandlerException(oops);
        }

    }

    /**
    * Now property definer is initialized by all properties and we can put
    * property value to context
    */
    public void postHandle(InterpretationContext interpretationContext, Model model) throws ModelHandlerException {
        if (inError) {
            return;
        }

        Object o = interpretationContext.peekObject();

        if (o != definer) {
            addWarn("The object at the of the stack is not the property definer for property named [" + propertyName + "] pushed earlier.");
        } else {
            interpretationContext.popObject();
            if (definer instanceof LifeCycle)
                ((LifeCycle) definer).start();

            // let's put defined property and value to context but only if it is
            // not null
            String propertyValue = definer.getPropertyValue();
            if (propertyValue != null) {
                addInfo("Setting property "+propertyName+"="+propertyValue+" in scope "+scope);
                ActionUtil.setProperty(interpretationContext, propertyName, propertyValue, scope);
            }
        }

    }
}
