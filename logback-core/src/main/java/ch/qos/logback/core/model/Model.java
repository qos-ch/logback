/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract representation of configuration elements
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.3.0
 */
public class Model implements Serializable {

    private static final long serialVersionUID = -797372668713068159L;

    // this state should not be here but should be treated via listeners
    // between processors and ModelHandlers
    boolean handled = false;
    boolean skipped = false;

    String tag;
    String bodyText;
    int lineNumber;

    List<Model> subModels = new ArrayList<>();

    static public Model duplicate(Model that) {
        Model copy = that.makeNewInstance();
        copy.mirror(that);
        for(Model m: that.subModels) {
            Model duplicate = duplicate(m);
            copy.subModels.add(duplicate);
        }
        return copy;
    }
    
    protected Model makeNewInstance() {
        return new Model();
    }
    
    protected void mirror(Model that) {
        this.tag = that.tag;
        this.bodyText = that.bodyText;
        this.lineNumber = that.lineNumber;
    }
    
    
    public void markAsSkipped() {
        skipped = true;
    }
    public void deepMarkAsSkipped() {
        markAsSkipped();
        for(Model m: this.getSubModels()) {
            m.deepMarkAsSkipped();
        }
    }
    /**
     * The model can re-used at reconfiguration time.
     * 
     * @since 1.3.0-alpha14
     */
    void resetForReuse() {
       this.handled = false;
       this.skipped = false;
       for(Model sub: subModels) {
           sub.resetForReuse();
       }
    }
    
    public boolean isSkipped() {
        return skipped;
    }

    public boolean isUnhandled() {
        return !handled;
    }

    public boolean isHandled() {
        return handled;
    }

    public void markAsHandled() {
        handled = true;
    }




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

    public String idString() {
        return "<" + tag + "> at line " + lineNumber;
    }


    @Override
    public int hashCode() {
        return Objects.hash(bodyText, lineNumber, subModels, tag);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Model other = (Model) obj;
        return Objects.equals(bodyText, other.bodyText) && lineNumber == other.lineNumber
                && Objects.equals(subModels, other.subModels) && Objects.equals(tag, other.tag);
    }

    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [tag=" + tag + ", bodyText=" + bodyText + ", id="+hashCode()+"]";
    }

}