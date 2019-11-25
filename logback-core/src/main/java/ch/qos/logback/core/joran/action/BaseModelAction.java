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
    public void begin(InterpretationContext intercon, String name, Attributes attributes) throws ActionException {
        parentModel = null;
        inError = false;
        
        if (!validPreconditions(intercon, name, attributes)) {
            inError = true;
            return;
        }
        
        currentModel = buildCurrentModel(intercon, name, attributes);
        currentModel.setTag(name);
        if(!intercon.isModelStackEmpty()) {
        	parentModel = intercon.peekModel();
        }
        final int lineNumber = getLineNumber(intercon);
        currentModel.setLineNumber(lineNumber);
        intercon.pushModel(currentModel);
    }

    
    abstract protected Model buildCurrentModel(InterpretationContext interpretationContext, String name, Attributes attributes);

    /**
     * Validate preconditions of this action.
     * 
     * By default, true is returned. Sub-classes should override appropriatelly.
     * 
     * @param interpretationContext
     * @param name
     * @param attributes
     * @return
     */
    protected boolean validPreconditions(InterpretationContext intercon, String name, Attributes attributes) {
    	return true;
    }

    @Override
    public void body(InterpretationContext ec, String body) {
    	currentModel.addText(body);
    }

    @Override
    public void end(InterpretationContext interpretationContext, String name) throws ActionException {
        if(inError)
            return;
        
        Model m = interpretationContext.peekModel();

        if (m != currentModel) {
            addWarn("The object at the of the stack is not the model [" + currentModel.idString() + "] pushed earlier.");
            addWarn("This is wholly unexpected.");
        } 
        
        // do not pop nor add to parent if there is no parent
        if(parentModel != null) {
            parentModel.addSubModel(currentModel);
            interpretationContext.popModel();
        }
    }
}
