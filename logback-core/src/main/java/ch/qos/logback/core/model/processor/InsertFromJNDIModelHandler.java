package ch.qos.logback.core.model.processor;

import javax.naming.NamingException;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.joran.action.ActionUtil.Scope;
import ch.qos.logback.core.model.InsertFromJNDIModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.util.JNDIUtil;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.model.ModelUtil;

public class InsertFromJNDIModelHandler extends ModelHandlerBase {

    public InsertFromJNDIModelHandler(Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new InsertFromJNDIModelHandler(context);
    }

    @Override
    protected Class<InsertFromJNDIModel> getSupportedModelClass() {
        return InsertFromJNDIModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        int errorCount = 0;

        InsertFromJNDIModel ifjm = (InsertFromJNDIModel) model;

        String envEntryName = mic.subst(ifjm.getEnvEntryName());
        String asKey = mic.subst(ifjm.getAs());

        String scopeStr = mic.subst(ifjm.getScopeStr());
        Scope scope = ActionUtil.stringToScope(scopeStr);

        String envEntryValue;

        if (OptionHelper.isNullOrEmpty(envEntryName)) {
            addError("[" + InsertFromJNDIModel.ENV_ENTRY_NAME_ATTR + "] missing");
            errorCount++;
        }

        if (OptionHelper.isNullOrEmpty(asKey)) {
            addError("[" + InsertFromJNDIModel.AS_ATTR + "] missing");
            errorCount++;
        }

        if (errorCount != 0) {
            return;
        }

        try {
            javax.naming.Context ctx = JNDIUtil.getInitialContext();
            envEntryValue = JNDIUtil.lookupString(ctx, envEntryName);
            if (OptionHelper.isNullOrEmpty(envEntryValue)) {
                addError("[" + envEntryName + "] has null or empty value");
            } else {
                addInfo("Setting variable [" + asKey + "] to [" + envEntryValue + "] in [" + scope + "] scope");
                ModelUtil.setProperty(mic, asKey, envEntryValue, scope);
            }
        } catch (NamingException e) {
            addError("Failed to lookup JNDI env-entry [" + envEntryName + "]");
        }

    }

}
