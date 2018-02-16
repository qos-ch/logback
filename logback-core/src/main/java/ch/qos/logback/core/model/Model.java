package ch.qos.logback.core.model;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.util.OptionHelper;

/**
 * Abstract representation of configuration elements
 * 
 * @author Ceki Gulcu
 * @SINCE 1.3.0
 */
public class Model {


    String tag;
    String className;
    String bodyText;
    List<Model> subModels = new ArrayList<>();

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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

    public boolean isComponentModel() {
        if (!OptionHelper.isEmpty(this.getClassName()))
            return true;
     
        if(bodyText == null || bodyText.isEmpty() || bodyText.trim().isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +" [tag=" + tag + ", className=" + className + ", bodyText=" + bodyText + "]";
    }


}