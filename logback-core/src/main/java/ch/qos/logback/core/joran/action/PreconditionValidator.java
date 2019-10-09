package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.OptionHelper;

public class PreconditionValidator extends ContextAwareBase {

	boolean valid = true;
	InterpretationContext intercon;
	Attributes attributes;
	String tag;

	public PreconditionValidator(ContextAware origin, InterpretationContext intercon, String name, Attributes attributes) {
		super(origin);
		this.setContext(origin.getContext());
		this.intercon = intercon;
		this.tag = name;
		this.attributes = attributes;
	}

	public PreconditionValidator validateClassAttribute() {
		return generic(Action.CLASS_ATTRIBUTE);
	}

	public PreconditionValidator validateNameAttribute() {
		return generic(Action.NAME_ATTRIBUTE);
	}

	public PreconditionValidator validateValueAttribute() {
		return generic(JoranConstants.VALUE_ATTR);
	}

	public PreconditionValidator validateRefAttribute() {
		return generic(JoranConstants.REF_ATTRIBUTE);
	}
	
	public PreconditionValidator generic(String attributeName) {
		String attributeValue = attributes.getValue(attributeName);
		if (OptionHelper.isNullOrEmpty(attributeValue)) {
			addError("Missing attribute [" + attributeName + "] in element [" + tag + "] near line " + Action.getLineNumber(intercon));
			this.valid = false;
		}
		return this;
	}

	public boolean isValid() {
		return valid;
	}


}
