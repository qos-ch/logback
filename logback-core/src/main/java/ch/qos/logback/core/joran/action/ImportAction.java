package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.ImportModel;
import ch.qos.logback.core.model.Model;

/**
 * Populates {@link ImportModel} based on XML input.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.3.0
 */
public class ImportAction extends BaseModelAction {

	@Override
	protected boolean validPreconditions(InterpretationContext intercon, String name,
			Attributes attributes) {
		PreconditionValidator pv = new PreconditionValidator(this, intercon, name, attributes);
		pv.validateClassAttribute();
		return pv.isValid();
	}
	
	@Override
	protected Model buildCurrentModel(InterpretationContext interpretationContext, String localName, Attributes attributes) {
		ImportModel importModel = new ImportModel();
		importModel.setClassName(attributes.getValue(CLASS_ATTRIBUTE));
		return importModel;
	}

}
