package ch.qos.logback.core.model.processor;

import java.util.HashMap;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.ActionConst;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.OptionHelper;

public class AppenderModelHandler<E> extends ModelHandlerBase {
	Appender<E> appender;
	private boolean inError = false;

	public AppenderModelHandler(Context context) {
		super(context);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void handle(InterpretationContext interpContext, Model model) throws ModelHandlerException {
		AppenderModel appenderModel = (AppenderModel) model;

		String className = appenderModel.getClassName();

		try {
			addInfo("About to instantiate appender of type [" + className + "]");

			appender = (Appender<E>) OptionHelper.instantiateByClassName(className, ch.qos.logback.core.Appender.class,
					context);
			appender.setContext(context);

			String appenderName = interpContext.subst(appenderModel.getName());
			// The execution context contains a bag which contains the appenders
			// created thus far.
			HashMap<String, Appender<E>> appenderBag = (HashMap<String, Appender<E>>) interpContext.getObjectMap()
					.get(ActionConst.APPENDER_BAG);

			// add the appender just created to the appender bag.
			appenderBag.put(appenderName, appender);

			interpContext.pushObject(appender);
		} catch (Exception oops) {
			inError = true;
			addError("Could not create an Appender of type [" + className + "].", oops);
			throw new ModelHandlerException(oops);
		}
	}

	public void postHandle(InterpretationContext interpContext, Model model) throws ModelHandlerException {
		if (inError) {
			return;
		}
	    if (appender instanceof LifeCycle) {
            ((LifeCycle) appender).start();
        }

        Object o = interpContext.peekObject();

        if (o != appender) {
            addWarn("The object at the of the stack is not the appender named [" + appender.getName() + "] pushed earlier.");
        } else {
        	interpContext.popObject();
        }
	}

}
