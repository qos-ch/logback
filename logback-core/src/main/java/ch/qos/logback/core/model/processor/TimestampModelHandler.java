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

public class TimestampModelHandler extends ModelHandlerBase {

    boolean inError = false;

    public TimestampModelHandler(final Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(final Context context, final InterpretationContext ic) {
        return new TimestampModelHandler(context);
    }

    @Override
    protected Class<TimestampModel> getSupportedModelClass() {
        return TimestampModel.class;
    }

    @Override
    public void handle(final InterpretationContext interpretationContext, final Model model) {
        final TimestampModel timestampModel = (TimestampModel) model;
        final String keyStr = timestampModel.getKey();
        if (OptionHelper.isNullOrEmpty(keyStr)) {
            addError("Attribute named [" + Action.KEY_ATTRIBUTE + "] cannot be empty");
            inError = true;
        }
        final String datePatternStr = timestampModel.getDatePattern();
        if (OptionHelper.isNullOrEmpty(datePatternStr)) {
            addError("Attribute named [" + TimestampAction.DATE_PATTERN_ATTRIBUTE + "] cannot be empty");
            inError = true;
        }

        final String timeReferenceStr = timestampModel.getTimeReference();
        long timeReference;
        if (TimestampModel.CONTEXT_BIRTH.equalsIgnoreCase(timeReferenceStr)) {
            addInfo("Using context birth as time reference.");
            timeReference = context.getBirthTime();
        } else {
            timeReference = System.currentTimeMillis();
            addInfo("Using current interpretation time, i.e. now, as time reference.");
        }

        if (inError) {
            return;
        }

        final String scopeStr = timestampModel.getScopeStr();
        final Scope scope = ActionUtil.stringToScope(scopeStr);

        final CachingDateFormatter sdf = new CachingDateFormatter(datePatternStr);
        final String val = sdf.format(timeReference);

        addInfo("Adding property to the context with key=\"" + keyStr + "\" and value=\"" + val + "\" to the " + scope + " scope");
        ActionUtil.setProperty(interpretationContext, keyStr, val, scope);

    }

}
