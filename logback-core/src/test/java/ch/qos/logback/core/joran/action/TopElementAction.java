package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.TopModel;

/**
 * Add a Model instance at the top of the InterpretationContext stack
 * 
 * @author Ceki Gulcu
 */
public class TopElementAction extends BaseModelAction {

	@Override
	protected Model buildCurrentModel(InterpretationContext interpretationContext, String name, Attributes attributes) {
        TopModel topModel = new TopModel();
        return topModel;
	}
	
}
