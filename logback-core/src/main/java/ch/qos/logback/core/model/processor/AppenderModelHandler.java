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
	AppenderAttachable<E> appenderAttachable;
	
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
		Map<String, AppenderAttachable<E>> appenderRefBag = (Map<String, AppenderAttachable<E>>) interpContext.getObjectMap()
				.get(JoranConstants.APPENDER_REF_BAG);
	
		this.appenderAttachable = appenderRefBag.get(appenderName);
		
		if(this.appenderAttachable == null) {
			addWarn("Appender named ["+appenderName+"] not referenced. Skipping further processing.");
			skipped = true;
			return;
		}
		
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
			inError = true;
			addError("Could not create an Appender of type [" + className + "].", oops);
			throw new ModelHandlerException(oops);
		}
	}

	public void postHandle(InterpretationContext interpContext, Model model) throws ModelHandlerException {
		if (inError || skipped) {
			return;
		}
	    if (appender instanceof LifeCycle) {
            ((LifeCycle) appender).start();
        }

        Object o = interpContext.peekObject();

        if (o != appender) {
            addWarn("The object at the of the stack is not the appender named [" + appender.getName() + "] pushed earlier.");
        } else {
        	addInfo("Attaching appender ["+appender.getName()+"] to "+appenderAttachable);
        	appenderAttachable.addAppender(appender);
        	
        	interpContext.popObject();
        }
	}

}
