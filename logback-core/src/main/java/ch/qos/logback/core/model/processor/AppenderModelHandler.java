package ch.qos.logback.core.model.processor;

import java.util.Map;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.OptionHelper;

public class AppenderModelHandler<E> extends ModelHandlerBase {
	Appender<E> appender;
	private boolean inError = false;
	private boolean skipped = false;
	
	public AppenderModelHandler(Context context) {
		super(context);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void handle(InterpretationContext interpContext, Model model) throws ModelHandlerException {
		this.appender = null;
		this.inError = false;
		
		AppenderModel appenderModel = (AppenderModel) model;

		String appenderName = interpContext.subst(appenderModel.getName());
		
		addInfo("Processing appender named ["+appenderName+"]");
		
		String className = appenderModel.getClassName();
		
		try {
			addInfo("About to instantiate appender of type [" + className + "]");

			appender = (Appender<E>) OptionHelper.instantiateByClassName(className, ch.qos.logback.core.Appender.class,
					context);
			appender.setContext(context);
            appender.setName(appenderName);
			interpContext.pushObject(appender);
		} catch (Exception oops) {
			addError("Could not create an Appender of type [" + className + "].", oops);
			throw new ModelHandlerException(oops);
		}
	}

    public void postHandle(InterpretationContext interpContext, Model model) throws ModelHandlerException {
        if (inError || skipped) {
            return;
        }

        Object o = interpContext.peekObject();

        if (o != appender) {
            addWarn("The object at the of the stack is not the appender named [" + appender.getName() + "] pushed earlier.");
        } else {
            interpContext.popObject();
        }
    }

    @Override
    public void postModelProcessing(InterpretationContext context) {
        if (appender == null) return;

        @SuppressWarnings("unchecked")
        Map<String, AppenderAttachable<E>> appenderRefBag = (Map<String, AppenderAttachable<E>>) context.getObjectMap()
                .get(JoranConstants.APPENDER_REF_BAG);
        String appenderName = appender.getName();
        AppenderAttachable<E> appenderAttachable = appenderRefBag.get(appenderName);

        if (appenderAttachable == null) {
            addWarn("Appender named [" + appenderName + "] not referenced. Skipping further processing.");
            skipped = true;
            return;
        }

        addInfo("Attaching appender [" + appender.getName() + "] to " + appenderAttachable);
        appenderAttachable.addAppender(appender);
    }

    @Override
    public void startLifeCycle() {
	    if (appender == null) return;
        appender.start();
    }
}
