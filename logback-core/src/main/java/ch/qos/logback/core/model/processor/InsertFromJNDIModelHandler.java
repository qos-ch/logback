package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.joran.action.ActionUtil.Scope;
import ch.qos.logback.core.model.InsertFromJNDIModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.util.PropertyModelHandlerHelper;
import ch.qos.logback.core.spi.ContextAwarePropertyContainer;
import ch.qos.logback.core.util.JNDIUtil;
import ch.qos.logback.core.util.OptionHelper;

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
        InsertFromJNDIModel ifjm = (InsertFromJNDIModel) model;
        detachedHandle(mic, ifjm);
    }

    /**
     *
     * @param capc
     * @param ifjm
     * @since 1.5.11
     */
    public void detachedHandle(ContextAwarePropertyContainer capc, InsertFromJNDIModel ifjm) {
        int errorCount = 0;
        String envEntryName = capc.subst(ifjm.getEnvEntryName());
        String asKey = capc.subst(ifjm.getAs());

        String scopeStr = capc.subst(ifjm.getScopeStr());
        Scope scope = ActionUtil.stringToScope(scopeStr);

        String envEntryValue;

        if (OptionHelper.isNullOrEmptyOrAllSpaces(envEntryName)) {
            addError("[" + InsertFromJNDIModel.ENV_ENTRY_NAME_ATTR + "] missing");
            errorCount++;
        }

        if (OptionHelper.isNullOrEmptyOrAllSpaces(asKey)) {
            addError("[" + InsertFromJNDIModel.AS_ATTR + "] missing");
            errorCount++;
        }

        if (errorCount != 0) {
            return;
        }

        try {
            javax.naming.Context ctx = JNDIUtil.getInitialContext();
            envEntryValue = JNDIUtil.lookupString(ctx, envEntryName);
            if (OptionHelper.isNullOrEmptyOrAllSpaces(envEntryValue)) {
                addError("[" + envEntryName + "] has null or empty value");
            } else {
                addInfo("Setting variable [" + asKey + "] to [" + envEntryValue + "] in [" + scope + "] scope");
                PropertyModelHandlerHelper.setProperty(capc, asKey, envEntryValue, scope);
            }
        } catch (Exception e) {
            addError("Failed to lookup JNDI env-entry [" + envEntryName + "]");
        }

    }

}
