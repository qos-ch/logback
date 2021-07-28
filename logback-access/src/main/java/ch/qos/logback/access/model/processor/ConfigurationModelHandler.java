package ch.qos.logback.access.model.processor;

import ch.qos.logback.access.model.ConfigurationModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StatusListenerConfigHelper;

public class ConfigurationModelHandler extends ModelHandlerBase {
	static final String DEBUG_SYSTEM_PROPERTY_KEY = "logback-access.debug";

	public ConfigurationModelHandler(Context context) {
		super(context);
	}
	
	static public ModelHandlerBase makeInstance(Context context, InterpretationContext ic) {
		return new ConfigurationModelHandler(context);
	}	
		

	protected Class<ConfigurationModel> getSupportedModelClass() {
		return ConfigurationModel.class;
	}

	@Override
	public void handle(InterpretationContext intercon, Model model) throws ModelHandlerException {
		ConfigurationModel configurationModel = (ConfigurationModel) model;
		// See LBCLASSIC-225 (the system property is looked up first. Thus, it overrides
		// the equivalent property in the config file. This reversal of scope priority
		// is justified
		// by the use case: the admin trying to chase rogue config file
		String debug = System.getProperty(DEBUG_SYSTEM_PROPERTY_KEY);
		if (debug == null) {
			debug = configurationModel.getDebug();
		}
		if (OptionHelper.isNullOrEmpty(debug) || debug.equals("false") || debug.equals("null")) {
			addInfo(ConfigurationModel.INTERNAL_DEBUG_ATTR + " attribute not set");
		} else {
			StatusListenerConfigHelper.addOnConsoleListenerInstance(context, new OnConsoleStatusListener());
		}

	}
}
