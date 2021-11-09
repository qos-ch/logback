package ch.qos.logback.core.joran.action;

import java.util.Stack;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.Model;

/**
 *
 * Action dealing with elements corresponding to implicit rules.
 *
 *
 * @author Ceki G&uuml;lc&uuml;
 *
 */
// TODO: rename to DefaultImplicitRuleAction (after Model migration)
public class ImplicitModelAction extends Action {

	Stack<ImplicitModel> currentImplicitModelStack = new Stack<>();

	@Override
	public void begin(final InterpretationContext interpretationContext, final String name, final Attributes attributes) throws ActionException {
		final ImplicitModel currentImplicitModel = new ImplicitModel();
		currentImplicitModel.setTag(name);

		String className = attributes.getValue(CLASS_ATTRIBUTE);
		if(className == null) {
			final String implicitClassName = interpretationContext.getDefaultNestedComponentRegistry().findDefaultComponentTypeByTag(name);
			if(implicitClassName != null) {
				addInfo("Assuming default class name ["+implicitClassName+"] for tag ["+name+"]");
				className = implicitClassName;
			}
		}
		currentImplicitModel.setClassName(className);
		currentImplicitModelStack.push(currentImplicitModel);
		interpretationContext.pushModel(currentImplicitModel);
	}

	@Override
	public void body(final InterpretationContext ec, final String body) {
		final ImplicitModel implicitModel = currentImplicitModelStack.peek();
		implicitModel.addText(body);
	}

	@Override
	public void end(final InterpretationContext interpretationContext, final String name) throws ActionException {

		final ImplicitModel implicitModel = currentImplicitModelStack.peek();
		final Model otherImplicitModel = interpretationContext.popModel();

		if(implicitModel != otherImplicitModel) {
			addError(implicitModel+ " does not match "+otherImplicitModel);
			return;
		}
		final Model parentModel = interpretationContext.peekModel();
		parentModel.addSubModel(implicitModel);
		currentImplicitModelStack.pop();

	}



}
