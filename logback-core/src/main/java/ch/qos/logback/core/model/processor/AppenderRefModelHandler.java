package ch.qos.logback.core.model.processor;

import java.util.Map;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.AppenderRefModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.AppenderAttachable;

public class AppenderRefModelHandler extends ModelHandlerBase {
	boolean inError = false;

	public AppenderRefModelHandler(Context context) {
		super(context);
	}

	static public ModelHandlerBase makeInstance(Context context, InterpretationContext ic) {
		return new AppenderRefModelHandler(context);
	}	
		
	@Override
	protected Class<? extends AppenderRefModel> getSupportedModelClass() {
		return AppenderRefModel.class;
	}

	@Override
	public void handle(InterpretationContext interpContext, Model model) throws ModelHandlerException {

		Object o = interpContext.peekObject();

		if (!(o instanceof AppenderAttachable)) {
			inError = true;
			String errMsg = "Could not find an AppenderAttachable at the top of execution stack. Near "
					+ model.idString();
			addError(errMsg);
			return;
		}

		AppenderRefModel appenderRefModel = (AppenderRefModel) model;
		AppenderAttachable<?> appenderAttachable = (AppenderAttachable<?>) o;

		attachRefencedAppenders(interpContext, appenderRefModel,appenderAttachable);
		
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	void attachRefencedAppenders(InterpretationContext interpContext, AppenderRefModel appenderRefModel, AppenderAttachable<?> appenderAttachable) {
		String appenderName = interpContext.subst(appenderRefModel.getRef());
		
		Map<String, Appender> appenderBag = (Map<String, Appender>) interpContext.getObjectMap()
				.get(JoranConstants.APPENDER_BAG);

		Appender appender = appenderBag.get(appenderName);
		if (appender == null) {
			addError("Failed to find appender named [" + appenderName + "]");
		} else {
			addInfo("Attaching appender named [" + appenderName + "] to " +appenderAttachable );
			appenderAttachable.addAppender(appender);
		}

	}
}
