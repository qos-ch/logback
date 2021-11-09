package ch.qos.logback.core.model.processor;

import static ch.qos.logback.core.joran.action.Action.CLASS_ATTRIBUTE;

import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.EventEvaluatorModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.OptionHelper;

public class EventEvaluatorModelHandler extends ModelHandlerBase {

    EventEvaluator<?> evaluator;
    boolean inError = false;

    public EventEvaluatorModelHandler(final Context context) {
        super(context);
    }

    static public ModelHandlerBase makeInstance(final Context context, final InterpretationContext ic) {
        return new EventEvaluatorModelHandler(context);
    }

    @Override
    protected Class<EventEvaluatorModel> getSupportedModelClass() {
        return EventEvaluatorModel.class;
    }

    @Override
    public void handle(final InterpretationContext intercon, final Model model) throws ModelHandlerException {
        final EventEvaluatorModel eem = (EventEvaluatorModel) model;

        String className = eem.getClassName();
        if (OptionHelper.isNullOrEmpty(className)) {
            final String defaultClassName = defaultClassName(intercon, eem);
            if (OptionHelper.isNullOrEmpty(defaultClassName)) {
                inError = true;
                addError("Mandatory \"" + CLASS_ATTRIBUTE + "\" attribute missing for <evaluator> at line "
                                + intercon.getLineNumber());
                addError("No default classname could be found.");
                return;
            }
            addInfo("Assuming default evaluator class [" + defaultClassName + "]");
            className = defaultClassName;
        }

        final String evaluatorName = intercon.subst(eem.getName());
        try {
            evaluator = (EventEvaluator<?>) OptionHelper.instantiateByClassName(className,
                            ch.qos.logback.core.boolex.EventEvaluator.class, context);
            evaluator.setContext(context);
            evaluator.setName(evaluatorName);
            intercon.pushObject(evaluator);

        } catch (final Exception oops) {
            inError = true;
            addError("Could not create evaluator of type " + className + "].", oops);
        }

    }

    private String defaultClassName(final InterpretationContext intercon, final EventEvaluatorModel model) {
        final DefaultNestedComponentRegistry registry = intercon.getDefaultNestedComponentRegistry();
        return registry.findDefaultComponentTypeByTag(model.getTag());
    }

    @Override
    public void postHandle(final InterpretationContext intercon, final Model model) throws ModelHandlerException {
        if (inError) {
            return;
        }

        if (evaluator instanceof LifeCycle) {
            ((LifeCycle) evaluator).start();
            addInfo("Starting evaluator named [" + evaluator.getName() + "]");
        }

        final Object o = intercon.peekObject();

        if (o != evaluator) {
            addWarn("The object on the top the of the stack is not the evaluator pushed earlier.");
        } else {
            intercon.popObject();

            try {
                @SuppressWarnings("unchecked")
                final
                Map<String, EventEvaluator<?>> evaluatorMap = (Map<String, EventEvaluator<?>>) context
                .getObject(CoreConstants.EVALUATOR_MAP);
                if (evaluatorMap == null) {
                    addError("Could not find EvaluatorMap");
                } else {
                    evaluatorMap.put(evaluator.getName(), evaluator);
                }
            } catch (final Exception ex) {
                addError("Could not set evaluator named [" + evaluator + "].", ex);
            }
        }
    }

}
