package ch.qos.logback.core.model;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.util.OptionHelper;

/**
 * Abstract representation of configuration elements
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.3.0
 */
public class Model {

    String tag;
    String bodyText;
    int lineNumber;
    
    List<Model> subModels = new ArrayList<>();

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public List<Model> getSubModels() {
        return subModels;
    }

    public void addSubModel(Model m) {
        subModels.add(m);
    }

    public String getBodyText() {
        return bodyText;
    }

    public void addText(String bodytext) {
        if (bodyText == null)
            this.bodyText = bodytext;
        else
            this.bodyText += bodytext;
    }

    // TODO: this method needs to be revised
    public boolean isComponentModel() {
    	if(this instanceof ComponentModel) {
    		ComponentModel componentModel = (ComponentModel) this;
            if (!OptionHelper.isEmpty(componentModel.getClassName()))
                return true;

            if(bodyText == null || bodyText.isEmpty() || bodyText.trim().isEmpty()) {
                return true;
            }
            return false;
    	} else {
    		return false;
    	}
    }


    public String idString() {
        return "<"+tag+"> at line "+lineNumber;
    }

    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() +" [tag=" + tag + ", bodyText=" + bodyText + "]";
    }


}