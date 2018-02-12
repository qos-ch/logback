package ch.qos.logback.core.joran.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract representation of configuration elements
 * 
 * @author Ceki Gulcu
 */
public class Model {

    String className;
    String bodyText;
    List<Parameter> parameters = new ArrayList<>();
    List<Model> subModels = new ArrayList<>();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void addParameter(Parameter p) {
        parameters.add(p);
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

}
