package ch.qos.logback.core.model.processor;

import java.util.HashMap;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.action.ActionConst;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.AppenderRefModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.spi.AppenderAttachable;

public class AppenderRefModelHandler extends ModelHandlerBase {
	boolean inError = false;

	public AppenderRefModelHandler(Context context) {
		super(context);
	}

	@Override
	protected Class<? extends AppenderRefModel> getSupportedModelClass() {
		return AppenderRefModel.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void handle(InterpretationContext intercon, Model model) throws ModelHandlerException {
		// logger.debug("begin called");

		Object o = intercon.peekObject(); 

		if (!(o instanceof AppenderAttachable)) {
			inError = true;
			String errMsg = "Could not find an AppenderAttachable at the top of execution stack. Near "
					+ model.idString();
			addError(errMsg);
			return;
		}

		AppenderRefModel appenderRefModel = (AppenderRefModel) model;

        AppenderAttachable<?> appenderAttachable = (AppenderAttachable<?>) o;

        String appenderName = intercon.subst(appenderRefModel.getRef());
		
		HashMap<String, Appender<?>> appenderBag = (HashMap<String, Appender<?>>) intercon.getObjectMap()
				.get(ActionConst.APPENDER_BAG);
		
		@SuppressWarnings("rawtypes")
		Appender appender = appenderBag.get(appenderName);

		if (appender == null) {
			String msg = "Could not find an appender named [" + appenderName
					+ "]. Did you define it below instead of above in the configuration file?";
			inError = true;
			addError(msg);
			addError("See " + CoreConstants.CODES_URL + "#appender_order for more details.");
			return;
		}

		addInfo("Attaching appender named [" + appenderName + "] to " + appenderAttachable);
		appenderAttachable.addAppender(appender);
	}

}
