package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.FilterReply;

public class AllowModelFilter implements ModelFiler {

	final Class<? extends Model> allowedModelType;

	AllowModelFilter(Class<? extends Model> allowedType) {
		this.allowedModelType = allowedType;
	}

	@Override
	public FilterReply decide(Model model) {

		if (model.getClass() == allowedModelType) {
			return FilterReply.ACCEPT;
		}

		return FilterReply.NEUTRAL;
	}

}
