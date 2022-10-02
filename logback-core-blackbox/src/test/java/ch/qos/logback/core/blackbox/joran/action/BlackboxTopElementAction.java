package ch.qos.logback.core.blackbox.joran.action;

import ch.qos.logback.core.blackbox.model.BlackboxTopModel;
import ch.qos.logback.core.joran.action.BaseModelAction;
import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;

/**
 * Add a Model instance at the top of the InterpretationContext stack
 * 
 * @author Ceki Gulcu
 */
public class BlackboxTopElementAction extends BaseModelAction {

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name,
            Attributes attributes) {
        BlackboxTopModel topModel = new BlackboxTopModel();
        return topModel;
    }

}
