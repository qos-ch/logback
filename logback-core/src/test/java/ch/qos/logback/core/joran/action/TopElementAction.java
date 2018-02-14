package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;

/**
 * Add a Model instance at the top of the InterpretationContext stack
 * 
 * @author Ceki Gulcu
 */
public class TopElementAction extends Action {

    public void begin(InterpretationContext interpretationContext, String name, Attributes attributes) {
        Model model = new Model();
        model.setTag(name);
        interpretationContext.pushObject(model);
    }

    public void end(InterpretationContext interpretationContext, String name) {
    }
}
