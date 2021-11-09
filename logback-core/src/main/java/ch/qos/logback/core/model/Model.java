package ch.qos.logback.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract representation of configuration elements
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.3.0
 */
public class Model  implements Serializable {

    private static final long serialVersionUID = -797372668713068159L;

    // this state should not be here but should be treated via listeners
    // between processors and ModelHandlers
    boolean handled = false;

    public boolean isUnhandled() {
        return !handled;
    }

    public boolean isHandled() {
        return handled;
    }
    public void markAsHandled() {
        handled = true;
    }

    String tag;
    String bodyText;
    int lineNumber;

    List<Model> subModels = new ArrayList<>();

    public String getTag() {
        return tag;
    }

    public void setTag(final String tag) {
        this.tag = tag;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(final int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public List<Model> getSubModels() {
        return subModels;
    }

    public void addSubModel(final Model m) {
        subModels.add(m);
    }

    public String getBodyText() {
        return bodyText;
    }

    public void addText(final String bodytext) {
        if (bodyText == null) {
            bodyText = bodytext;
        } else {
            bodyText += bodytext;
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