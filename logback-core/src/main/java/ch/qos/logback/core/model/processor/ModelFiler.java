package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.FilterReply;

public interface ModelFiler {

	
	FilterReply decide(Model model);
	
}
