package ch.qos.logback.core.model.processor;

import java.util.Map;

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

	@Override
	protected Class<? extends AppenderRefModel> getSupportedModelClass() {
		return AppenderRefModel.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void handle(InterpretationContext intercon, Model model) throws ModelHandlerException {
		
		
		//should be NOP
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
		
		Map<String, AppenderAttachable<?>> appenderRefBag = (Map<String, AppenderAttachable<?>>) intercon.getObjectMap()
				.get(JoranConstants.APPENDER_REF_BAG);
		
		appenderRefBag.put(appenderName, appenderAttachable);

	}

}
