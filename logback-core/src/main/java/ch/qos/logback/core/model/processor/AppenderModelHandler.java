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

	public AppenderModelHandler(final Context context) {
		super(context);
	}

	@SuppressWarnings("rawtypes")
	static public ModelHandlerBase makeInstance(final Context context, final InterpretationContext ic) {
		return new AppenderModelHandler(context);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void handle(final InterpretationContext interpContext, final Model model) throws ModelHandlerException {
		this.appender = null;
		this.inError = false;

		final AppenderModel appenderModel = (AppenderModel) model;

		final String appenderName = interpContext.subst(appenderModel.getName());

		if(!interpContext.hasDependencies(appenderName)) {
			addWarn("Appender named ["+appenderName+"] not referenced. Skipping further processing.");
			skipped = true;
			return;
		}

		//		//this.appenderAttachable = appenderRefBag.get(appenderName);
		//
		//		if(this.appenderAttachable == null) {
		//			addWarn("Appender named ["+appenderName+"] not referenced. Skipping further processing.");
		//			skipped = true;
		//			return;
		//		}

		addInfo("Processing appender named ["+appenderName+"]");

		final String className = appenderModel.getClassName();

		try {
			addInfo("About to instantiate appender of type [" + className + "]");

			appender = (Appender<E>) OptionHelper.instantiateByClassName(className, ch.qos.logback.core.Appender.class,
					context);
			appender.setContext(context);
			appender.setName(appenderName);
			interpContext.pushObject(appender);
		} catch (final Exception oops) {
			inError = true;
			addError("Could not create an Appender of type [" + className + "].", oops);
			throw new ModelHandlerException(oops);
		}
	}

	@Override
	public void postHandle(final InterpretationContext interpContext, final Model model) throws ModelHandlerException {
		if (inError || skipped) {
			return;
		}
		if (appender instanceof LifeCycle) {
			((LifeCycle) appender).start();
		}
		interpContext.markStartOfNamedDependency(appender.getName());

		final Object o = interpContext.peekObject();

		@SuppressWarnings("unchecked")
		final
		Map<String, Appender<E>> appenderBag = (Map<String, Appender<E>>) interpContext.getObjectMap()
		.get(JoranConstants.APPENDER_BAG);
		appenderBag.put(appender.getName(), appender);

		if (o != appender) {
			addWarn("The object at the of the stack is not the appender named [" + appender.getName() + "] pushed earlier.");
		} else {
			//        	addInfo("Attaching appender ["+appender.getName()+"] to "+appenderAttachable);
			//        	appenderAttachable.addAppender(appender);
			interpContext.popObject();
		}

	}

}
