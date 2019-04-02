package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.OptionHelper;

public class PreconditionValidator extends ContextAwareBase {

	boolean valid = true;
	InterpretationContext ic;
	Attributes attributes;
	String tag;

	PreconditionValidator(ContextAware origin, InterpretationContext ic, String name, Attributes attributes) {
		super(origin);
		this.setContext(origin.getContext());
		this.ic = ic;
		this.tag = name;
		this.attributes = attributes;
	}

	PreconditionValidator validateClassAttribute() {
		return generic(Action.CLASS_ATTRIBUTE);
	}

	PreconditionValidator validateNameAttribute() {
		return generic(Action.NAME_ATTRIBUTE);
	}

	PreconditionValidator generic(String attributeName) {
		String attributeValue = attributes.getValue(attributeName);
		if (OptionHelper.isEmpty(attributeValue)) {
			addError("Missing attribute [" + attributeName + "] in element [" + tag + "] near line " + Action.getLineNumber(ic));
			this.valid = false;
		}
		return this;
	}

	public boolean isValid() {
		return valid;
	}
}
