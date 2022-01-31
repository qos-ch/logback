package ch.qos.logback.core.model;

import java.util.Properties;

import ch.qos.logback.core.joran.action.ActionUtil.Scope;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.util.ContextUtil;
import ch.qos.logback.core.util.OptionHelper;

public class ModelUtil {

    
    static public void resetForReuse(Model model) {
        if(model == null)
           return;
        model.resetForReuse();
    }
    
    /**
     * Add all the properties found in the argument named 'props' to an
     * InterpretationContext.
     */
    static public void setProperty(ModelInterpretationContext mic, String key, String value, Scope scope) {
        switch (scope) {
        case LOCAL:
            mic.addSubstitutionProperty(key, value);
            break;
        case CONTEXT:
            mic.getContext().putProperty(key, value);
            break;
        case SYSTEM:
            OptionHelper.setSystemProperty(mic, key, value);
        }
    }

    /**
     * Add all the properties found in the argument named 'props' to an
     * InterpretationContext.
     */
    static public void setProperties(ModelInterpretationContext ic, Properties props, Scope scope) {
        switch (scope) {
        case LOCAL:
            ic.addSubstitutionProperties(props);
            break;
        case CONTEXT:
            ContextUtil cu = new ContextUtil(ic.getContext());
            cu.addProperties(props);
            break;
        case SYSTEM:
            OptionHelper.setSystemProperties(ic, props);
        }
    }
}
