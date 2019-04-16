package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.FilterReply;

public class DenyModelFilter implements ModelFiler {

	final Class<? extends Model> deniedModelType;

	DenyModelFilter(Class<? extends Model> deniedModelType) {
		this.deniedModelType = deniedModelType;
	}

	@Override
	public FilterReply decide(Model model) {

		if (model.getClass() == deniedModelType) {
			return FilterReply.DENY;
		}

		return FilterReply.NEUTRAL;
	}
	
	

}
