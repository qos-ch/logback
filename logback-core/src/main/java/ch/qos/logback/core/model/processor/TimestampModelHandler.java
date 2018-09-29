package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.joran.action.ActionUtil.Scope;
import ch.qos.logback.core.joran.action.TimestampAction;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.TimestampModel;
import ch.qos.logback.core.util.CachingDateFormatter;
import ch.qos.logback.core.util.OptionHelper;

public class TimestampModelHandler  extends ModelHandlerBase {

    boolean inError = false;
    
    public TimestampModelHandler(Context context) {
        super(context);
    }

    @Override
    public void handle(InterpretationContext interpretationContext, Model model) {
        if(!(model instanceof TimestampModel)) {
            addError("Can only handle models of type [" + TimestampModel.class + "]");
            return;
        }
        TimestampModel timestampModel = (TimestampModel) model;
        String keyStr = timestampModel.getKey();
        if (OptionHelper.isEmpty(keyStr)) {
            addError("Attribute named [" + Action.KEY_ATTRIBUTE + "] cannot be empty");
            inError = true;
        }
        String datePatternStr = timestampModel.getDatePattern();
        if (OptionHelper.isEmpty(datePatternStr)) {
            addError("Attribute named [" + TimestampAction.DATE_PATTERN_ATTRIBUTE + "] cannot be empty");
            inError = true;
        }

        String timeReferenceStr = timestampModel.getTimeReference();
        long timeReference;
        if (TimestampModel.CONTEXT_BIRTH.equalsIgnoreCase(timeReferenceStr)) {
            addInfo("Using context birth as time reference.");
            timeReference = context.getBirthTime();
        } else {
            timeReference = System.currentTimeMillis();
            addInfo("Using current interpretation time, i.e. now, as time reference.");
        }

        if (inError)
            return;

        String scopeStr = timestampModel.getScopeStr();
        Scope scope = ActionUtil.stringToScope(scopeStr);

        CachingDateFormatter sdf = new CachingDateFormatter(datePatternStr);
        String val = sdf.format(timeReference);

        addInfo("Adding property to the context with key=\"" + keyStr + "\" and value=\"" + val + "\" to the " + scope + " scope");
        ActionUtil.setProperty(interpretationContext, keyStr, val, scope);
        
    }

}
