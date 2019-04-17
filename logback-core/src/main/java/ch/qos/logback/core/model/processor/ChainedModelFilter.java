package ch.qos.logback.core.model.processor;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.FilterReply;

public class ChainedModelFilter implements ModelFiler {

	List<ModelFiler> modelFilters = new ArrayList<>();

	public  ChainedModelFilter() {
	}

	static public ChainedModelFilter newInstance() {
		return new ChainedModelFilter();
	}
	
	public  ChainedModelFilter allow(Class<? extends Model> allowedType) {
		modelFilters.add(new AllowModelFilter(allowedType));
		return this;
	}

	public  ChainedModelFilter deny(Class<? extends Model> allowedType) {
		modelFilters.add(new DenyModelFilter(allowedType));
		return this;
	}

	public ChainedModelFilter denyAll() {
		modelFilters.add(new DenyAllModelFilter());
		return this;
	}


	public  ChainedModelFilter allowAll() {
		modelFilters.add(new AllowAllModelFilter());
		return this;
	}

	@Override
	public FilterReply decide(Model model) {
		
		for(ModelFiler modelFilter: modelFilters) {
			FilterReply reply = modelFilter.decide(model);
			
			switch(reply) {
			case ACCEPT:
			case DENY:
				return reply;
			case NEUTRAL:
				// next
			}
		}
		return FilterReply.NEUTRAL;
	}

}
