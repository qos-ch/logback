package ch.qos.logback.core.joran;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.util.PropertySetter;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ParamModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;

public class ParamModelHandler extends ModelHandlerBase {

	private final BeanDescriptionCache beanDescriptionCache;

	public ParamModelHandler(Context context, InterpretationContext interpretationContext) {
		this(context, interpretationContext.getBeanDescriptionCache());
	}
	
	public ParamModelHandler(Context context, BeanDescriptionCache beanDescriptionCache) {
		super(context);
		this.beanDescriptionCache = beanDescriptionCache;
	}

	@Override
	protected Class<ParamModel> getSupportedModelClass() {
		return ParamModel.class;
	}

	@Override
	public void handle(InterpretationContext intercon, Model model) throws ModelHandlerException {

		ParamModel paramModel = (ParamModel) model;

		String valueStr = intercon.subst(paramModel.getValue());

		Object o = intercon.peekObject();

		PropertySetter propSetter = new PropertySetter(beanDescriptionCache, o);
		propSetter.setContext(context);

		// allow for variable substitution for name as well
		String finalName = intercon.subst(paramModel.getName());
	     propSetter.setProperty(finalName, valueStr);
	}

}
