package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;

public abstract class BaseModelAction extends Action {

    Model parentModel;
    Model currentModel;
    boolean inError = false;

    @Override
    public void begin(InterpretationContext interpretationContext, String name, Attributes attributes) throws ActionException {
        parentModel = null;
        inError = false;
        
        if (!validPreconditions(interpretationContext, name, attributes)) {
            inError = true;
            return;
        }
        parentModel = interpretationContext.peekModel();
        currentModel = buildCurrentModel(interpretationContext, name, attributes);
        currentModel.setTag(name);
        final int lineNumber = getLineNumber(interpretationContext);
        currentModel.setLineNumber(lineNumber);
        interpretationContext.pushModel(currentModel);
    }

    abstract protected Model buildCurrentModel(InterpretationContext interpretationContext, String name, Attributes attributes);

    abstract protected boolean validPreconditions(InterpretationContext interpretationContext, String name, Attributes attributes);

    @Override
    public void end(InterpretationContext interpretationContext, String name) throws ActionException {
        if(inError)
            return;
        
        Model m = interpretationContext.peekModel();

        if (m != currentModel) {
            addWarn("The object at the of the stack is not the model [" + currentModel.getTag() + "] pushed earlier.");
        } else {
            parentModel.addSubModel(currentModel);
            interpretationContext.popModel();
        }
    }
}
